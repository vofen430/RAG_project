package com.rag.service;

import com.rag.model.DocumentChunk;
import com.rag.model.DocumentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RagPipelineService {

    private static final Logger log = LoggerFactory.getLogger(RagPipelineService.class);

    private final DocumentService documentService;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private final RetrievalService retrievalService;
    private final RerankService rerankService;
    private final PromptService promptService;
    private final ChatService chatService;

    public RagPipelineService(DocumentService documentService, ChunkingService chunkingService,
                              EmbeddingService embeddingService, VectorStore vectorStore,
                              RetrievalService retrievalService, RerankService rerankService,
                              PromptService promptService, ChatService chatService) {
        this.documentService = documentService;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        this.retrievalService = retrievalService;
        this.rerankService = rerankService;
        this.promptService = promptService;
        this.chatService = chatService;
    }

    /**
     * Run the indexing pipeline (Ingestion -> Chunking -> Embedding -> Indexing).
     * This is run asynchronously after document upload.
     */
    public void indexDocument(String documentId) {
        CompletableFuture.runAsync(() -> {
            try {
                DocumentInfo info = documentService.getDocumentInfo(documentId);
                String text = documentService.getDocumentText(documentId);

                if (text == null || text.isEmpty()) {
                    info.setStatus("ERROR");
                    info.setErrorMessage("Document text is empty");
                    documentService.updateDocumentInfo(documentId, info);
                    return;
                }

                // Step 1: Chunking
                info.setStatus("CHUNKING");
                documentService.updateDocumentInfo(documentId, info);
                log.info("Chunking document {}", documentId);

                List<DocumentChunk> chunks = chunkingService.chunkDocument(documentId, text);
                info.setTotalChunks(chunks.size());
                documentService.updateDocumentInfo(documentId, info);
                log.info("Created {} chunks for document {}", chunks.size(), documentId);

                // Step 2: Embedding
                info.setStatus("EMBEDDING");
                documentService.updateDocumentInfo(documentId, info);
                log.info("Embedding {} chunks", chunks.size());

                List<String> texts = chunks.stream().map(DocumentChunk::getContent).toList();
                List<double[]> embeddings = embeddingService.embedBatch(texts);

                for (int i = 0; i < chunks.size(); i++) {
                    chunks.get(i).setEmbedding(embeddings.get(i));
                    info.setProcessedChunks(i + 1);
                    documentService.updateDocumentInfo(documentId, info);
                }

                // Step 3: Indexing
                info.setStatus("INDEXING");
                documentService.updateDocumentInfo(documentId, info);

                for (DocumentChunk chunk : chunks) {
                    vectorStore.addChunk(chunk);
                }

                info.setStatus("INDEXED");
                documentService.updateDocumentInfo(documentId, info);
                log.info("Document {} indexed successfully with {} chunks", documentId, chunks.size());

            } catch (Exception e) {
                log.error("Indexing failed for document {}: {}", documentId, e.getMessage(), e);
                DocumentInfo info = documentService.getDocumentInfo(documentId);
                if (info != null) {
                    info.setStatus("ERROR");
                    info.setErrorMessage(e.getMessage());
                    documentService.updateDocumentInfo(documentId, info);
                }
            }
        });
    }

    /**
     * Run the query pipeline (User Query -> Query Embedding -> Top-K Retrieval -> Reranking -> Prompt Construction -> LLM Generation).
     * Returns a streaming Flux of content tokens.
     */
    public Flux<String> query(String userQuery) {
        try {
            // Step 1: Query Embedding + Top-K Retrieval
            log.info("Retrieving relevant chunks for query: {}", userQuery);
            List<DocumentChunk> retrievedChunks = retrievalService.retrieve(userQuery);
            log.info("Retrieved {} chunks", retrievedChunks.size());

            if (retrievedChunks.isEmpty()) {
                return Flux.just("没有找到与您的问题相关的文档内容。请先上传并索引文档，然后再提问。");
            }

            // Step 2: Reranking
            log.info("Reranking {} chunks", retrievedChunks.size());
            List<DocumentChunk> rerankedChunks = rerankService.rerank(userQuery, retrievedChunks);
            log.info("Reranked to {} chunks", rerankedChunks.size());

            // Step 3: Prompt Construction
            String systemPrompt = promptService.buildSystemPrompt();
            String userMessage = promptService.buildUserMessage(userQuery, rerankedChunks);
            log.info("Constructed prompt with {} context chunks", rerankedChunks.size());

            // Step 4: LLM Generation (streaming)
            return chatService.streamChat(systemPrompt, userMessage);

        } catch (Exception e) {
            log.error("Query pipeline failed: {}", e.getMessage(), e);
            return Flux.just("查询处理失败: " + e.getMessage());
        }
    }
}
