package com.rag.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rag.entity.*;
import com.rag.mapper.*;
import com.rag.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Orchestrator for the full RAG pipeline with persistent storage.
 */
@Service
public class RagPipelineService {

    private static final Logger log = LoggerFactory.getLogger(RagPipelineService.class);

    private final DocumentService documentService;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final RetrievalService retrievalService;
    private final RerankService rerankService;
    private final PromptService promptService;
    private final ChatService chatService;
    private final IndexingJobService indexingJobService;
    private final DocumentChunkMapper documentChunkMapper;
    private final RagQueryTraceMapper traceMapper;
    private final TraceEvidenceItemMapper evidenceItemMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserSettingsMapper userSettingsMapper;
    private final SecurityUtil securityUtil;

    public RagPipelineService(DocumentService documentService, ChunkingService chunkingService,
                              EmbeddingService embeddingService, RetrievalService retrievalService,
                              RerankService rerankService, PromptService promptService,
                              ChatService chatService, IndexingJobService indexingJobService,
                              DocumentChunkMapper documentChunkMapper,
                              RagQueryTraceMapper traceMapper,
                              TraceEvidenceItemMapper evidenceItemMapper,
                              ChatMessageMapper chatMessageMapper,
                              UserSettingsMapper userSettingsMapper,
                              SecurityUtil securityUtil) {
        this.documentService = documentService;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.retrievalService = retrievalService;
        this.rerankService = rerankService;
        this.promptService = promptService;
        this.chatService = chatService;
        this.indexingJobService = indexingJobService;
        this.documentChunkMapper = documentChunkMapper;
        this.traceMapper = traceMapper;
        this.evidenceItemMapper = evidenceItemMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.userSettingsMapper = userSettingsMapper;
        this.securityUtil = securityUtil;
    }

    /**
     * Run the indexing pipeline asynchronously.
     */
    public IndexingJobEntity indexDocument(String documentId) {
        DocumentEntity doc = documentService.getDocument(documentId);
        IndexingJobEntity job = indexingJobService.createJob(documentId);

        CompletableFuture.runAsync(() -> {
            try {
                String text = documentService.readDocumentText(doc);
                if (text == null || text.isEmpty()) {
                    indexingJobService.markFailed(job, "Document text is empty");
                    doc.setDocumentStatus("ERROR");
                    doc.setErrorMessage("Document text is empty");
                    documentService.updateDocument(doc);
                    return;
                }

                // Get user settings for chunking parameters
                UserSettingsEntity settings = getUserSettings(doc.getUserId());
                int chunkSize = settings != null ? settings.getChunkSize() : 500;
                int chunkOverlap = settings != null ? settings.getChunkOverlap() : 100;
                String embeddingModel = settings != null ? settings.getEmbeddingModel() : "Qwen/Qwen3-Embedding-0.6B";

                // Step 1: Chunking
                job.setCurrentStage("CHUNKING");
                indexingJobService.updateJob(job);
                doc.setDocumentStatus("INDEXING");
                documentService.updateDocument(doc);
                log.info("Chunking document {}", documentId);

                List<DocumentChunkEntity> chunks = chunkingService.chunkDocument(
                        documentId, text, chunkSize, chunkOverlap, embeddingModel);
                job.setTotalChunks(chunks.size());
                indexingJobService.updateJob(job);
                log.info("Created {} chunks for document {}", chunks.size(), documentId);

                // Step 2: Embedding
                job.setCurrentStage("EMBEDDING");
                indexingJobService.updateJob(job);
                log.info("Embedding {} chunks", chunks.size());

                List<String> texts = chunks.stream().map(DocumentChunkEntity::getContentText).toList();
                List<double[]> embeddings = embeddingService.embedBatch(texts, embeddingModel);

                // Step 3: Vector Persistence
                job.setCurrentStage("PERSISTING");
                indexingJobService.updateJob(job);

                // Delete old chunks for this document first
                documentChunkMapper.deleteByDocumentId(documentId);

                for (int i = 0; i < chunks.size(); i++) {
                    DocumentChunkEntity chunk = chunks.get(i);
                    chunk.setEmbeddingVector(embeddings.get(i));
                    String vectorStr = RetrievalService.toVectorString(embeddings.get(i));
                    documentChunkMapper.insertWithVector(chunk, vectorStr);

                    job.setProcessedChunks(i + 1);
                    indexingJobService.updateJob(job);
                }

                // Mark complete
                indexingJobService.markCompleted(job, chunks.size());
                doc.setDocumentStatus("INDEXED");
                documentService.updateDocument(doc);
                log.info("Document {} indexed successfully with {} chunks", documentId, chunks.size());

            } catch (Exception e) {
                log.error("Indexing failed for document {}: {}", documentId, e.getMessage(), e);
                indexingJobService.markFailed(job, e.getMessage());
                doc.setDocumentStatus("ERROR");
                doc.setErrorMessage(e.getMessage());
                documentService.updateDocument(doc);
            }
        });

        return job;
    }

    /**
     * Run the query pipeline with trace persistence.
     * Returns a Flux of SSE event strings.
     */
    public QueryResult query(String sessionId, String userQuery, List<String> documentIds, String userId) {
        long startTime = System.currentTimeMillis();

        // Get user settings
        UserSettingsEntity settings = getUserSettings(userId);
        int topK = settings != null ? settings.getTopK() : 10;
        int topN = settings != null ? settings.getTopN() : 5;

        // Create trace record
        RagQueryTraceEntity trace = new RagQueryTraceEntity();
        trace.setId(UUID.randomUUID().toString());
        trace.setSessionId(sessionId);
        trace.setUserId(userId);
        trace.setUserQuery(userQuery);
        trace.setRetrievalTopK(topK);
        trace.setRerankTopN(topN);
        trace.setPromptVersion(PromptService.PROMPT_VERSION);
        trace.setAnswerStatus("PROCESSING");
        traceMapper.insert(trace);

        try {
            // Step 1: Retrieval
            String embeddingModel = settings != null ? settings.getEmbeddingModel() : "Qwen/Qwen3-Embedding-0.6B";
            log.info("Retrieving chunks for query in session {}", sessionId);
            List<DocumentChunkEntity> retrievedChunks;
            if (documentIds != null && !documentIds.isEmpty()) {
                retrievedChunks = retrievalService.retrieveByDocIds(userQuery, topK, documentIds, embeddingModel);
            } else {
                retrievedChunks = retrievalService.retrieve(userQuery, topK, userId, embeddingModel);
            }
            log.info("Retrieved {} chunks", retrievedChunks.size());

            if (retrievedChunks.isEmpty()) {
                trace.setAnswerStatus("NO_RESULTS");
                trace.setAnswerText("No relevant documents found.");
                trace.setLatencyMs((int) (System.currentTimeMillis() - startTime));
                traceMapper.updateById(trace);

                return new QueryResult(
                        trace.getId(),
                        Flux.just("No relevant documents found. Please upload and index documents first.")
                );
            }

            // Step 2: Reranking
            log.info("Reranking {} chunks", retrievedChunks.size());
            String rerankModel = settings != null ? settings.getRerankModel() : "Qwen/Qwen3-Reranker-0.6B";
            List<RerankService.RerankResult> reranked = rerankService.rerank(userQuery, retrievedChunks, topN, rerankModel);
            log.info("Reranked to {} evidence items", reranked.size());

            // Persist evidence items
            for (int i = 0; i < retrievedChunks.size(); i++) {
                DocumentChunkEntity chunk = retrievedChunks.get(i);
                TraceEvidenceItemEntity item = new TraceEvidenceItemEntity();
                item.setId(UUID.randomUUID().toString());
                item.setTraceId(trace.getId());
                item.setDocumentId(chunk.getDocumentId());
                item.setChunkId(chunk.getId());
                item.setRetrievalRank(i + 1);
                item.setRetrievalScore(BigDecimal.valueOf(0.0)); // Score comes from pgvector
                item.setIsSelected(false);
                evidenceItemMapper.insert(item);
            }

            // Mark selected evidence items with rerank scores
            for (int i = 0; i < reranked.size(); i++) {
                RerankService.RerankResult rr = reranked.get(i);
                // Find the evidence item for this chunk and update it
                LambdaQueryWrapper<TraceEvidenceItemEntity> q = new LambdaQueryWrapper<TraceEvidenceItemEntity>()
                        .eq(TraceEvidenceItemEntity::getTraceId, trace.getId())
                        .eq(TraceEvidenceItemEntity::getChunkId, rr.chunk().getId());
                TraceEvidenceItemEntity evidenceItem = evidenceItemMapper.selectOne(q);
                if (evidenceItem != null) {
                    evidenceItem.setRerankRank(rr.rerankRank());
                    evidenceItem.setRerankScore(rr.rerankScore());
                    evidenceItem.setIsSelected(true);
                    evidenceItem.setCitationNo(i + 1);
                    evidenceItemMapper.updateById(evidenceItem);
                }
            }

            // Step 3: Prompt Assembly
            String systemPrompt = promptService.buildSystemPrompt();
            String userMessage = promptService.buildUserMessage(userQuery, reranked);
            log.info("Constructed prompt with {} evidence items", reranked.size());

            // Step 4: LLM Streaming
            StringBuilder fullAnswer = new StringBuilder();
            String traceId = trace.getId();

            String chatModel = settings != null ? settings.getChatModel() : "Qwen/Qwen3-8B";
            Flux<String> stream = chatService.streamChat(systemPrompt, userMessage, chatModel)
                    .doOnNext(fullAnswer::append)
                    .doOnComplete(() -> {
                        // Persist final answer
                        RagQueryTraceEntity t = traceMapper.selectById(traceId);
                        if (t != null) {
                            t.setAnswerStatus("COMPLETED");
                            t.setAnswerText(fullAnswer.toString());
                            t.setLatencyMs((int) (System.currentTimeMillis() - startTime));
                            traceMapper.updateById(t);
                        }
                    })
                    .doOnError(e -> {
                        RagQueryTraceEntity t = traceMapper.selectById(traceId);
                        if (t != null) {
                            t.setAnswerStatus("FAILED");
                            t.setErrorMessage(e.getMessage());
                            t.setLatencyMs((int) (System.currentTimeMillis() - startTime));
                            traceMapper.updateById(t);
                        }
                    });

            return new QueryResult(traceId, stream);

        } catch (Exception e) {
            log.error("Query pipeline failed: {}", e.getMessage(), e);
            trace.setAnswerStatus("FAILED");
            trace.setErrorMessage(e.getMessage());
            trace.setLatencyMs((int) (System.currentTimeMillis() - startTime));
            traceMapper.updateById(trace);

            return new QueryResult(
                    trace.getId(),
                    Flux.just("Query processing failed: " + e.getMessage())
            );
        }
    }

    private UserSettingsEntity getUserSettings(String userId) {
        return userSettingsMapper.selectOne(
                new LambdaQueryWrapper<UserSettingsEntity>()
                        .eq(UserSettingsEntity::getUserId, userId)
        );
    }

    public record QueryResult(String traceId, Flux<String> stream) {}
}
