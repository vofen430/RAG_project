package com.rag.service;

import com.rag.model.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class VectorStore {

    private final Map<String, DocumentChunk> store = new ConcurrentHashMap<>();

    /**
     * Index a chunk with its embedding vector.
     */
    public void addChunk(DocumentChunk chunk) {
        if (chunk.getEmbedding() != null && chunk.getEmbedding().length > 0) {
            store.put(chunk.getChunkId(), chunk);
        }
    }

    /**
     * Find the top-K most similar chunks to the query embedding using cosine similarity.
     */
    public List<DocumentChunk> searchSimilar(double[] queryEmbedding, int topK) {
        if (queryEmbedding == null || queryEmbedding.length == 0) {
            return Collections.emptyList();
        }

        return store.values().stream()
                .filter(chunk -> chunk.getEmbedding() != null && chunk.getEmbedding().length == queryEmbedding.length)
                .map(chunk -> new AbstractMap.SimpleEntry<>(chunk, cosineSimilarity(queryEmbedding, chunk.getEmbedding())))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Cosine similarity between two vectors.
     */
    private double cosineSimilarity(double[] a, double[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0 ? 0.0 : dotProduct / denominator;
    }

    /**
     * Get total number of indexed chunks.
     */
    public int size() {
        return store.size();
    }

    /**
     * Remove all chunks for a specific document.
     */
    public void removeDocument(String documentId) {
        store.entrySet().removeIf(entry -> entry.getValue().getDocumentId().equals(documentId));
    }

    /**
     * Clear all chunks.
     */
    public void clear() {
        store.clear();
    }
}
