package com.rag.service;

import com.rag.entity.DocumentChunkEntity;
import com.rag.mapper.DocumentChunkMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Retrieval service using pgvector for similarity search.
 * Replaces the old in-memory VectorStore approach.
 */
@Service
public class RetrievalService {

    private static final Logger log = LoggerFactory.getLogger(RetrievalService.class);

    private final EmbeddingService embeddingService;
    private final DocumentChunkMapper documentChunkMapper;

    public RetrievalService(EmbeddingService embeddingService, DocumentChunkMapper documentChunkMapper) {
        this.embeddingService = embeddingService;
        this.documentChunkMapper = documentChunkMapper;
    }

    /**
     * Retrieve top-K similar chunks from pgvector, scoped to user's documents.
     */
    public List<DocumentChunkEntity> retrieve(String query, int topK, String userId) {
        double[] queryEmbedding = embeddingService.embedText(query);
        String vectorString = toVectorString(queryEmbedding);
        List<DocumentChunkEntity> results = documentChunkMapper.searchSimilar(vectorString, topK, userId);
        log.info("Retrieved {} chunks for user {} with topK={}", results.size(), userId, topK);
        return results;
    }

    /**
     * Retrieve top-K similar chunks scoped to specific document IDs.
     */
    public List<DocumentChunkEntity> retrieveByDocIds(String query, int topK, List<String> documentIds) {
        double[] queryEmbedding = embeddingService.embedText(query);
        String vectorString = toVectorString(queryEmbedding);
        List<DocumentChunkEntity> results = documentChunkMapper.searchSimilarByDocIds(vectorString, topK, documentIds);
        log.info("Retrieved {} chunks from {} documents with topK={}", results.size(), documentIds.size(), topK);
        return results;
    }

    /**
     * Convert a double array to a pgvector-compatible string: [0.1,0.2,0.3]
     */
    public static String toVectorString(double[] vector) {
        return "[" + Arrays.stream(vector)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }
}
