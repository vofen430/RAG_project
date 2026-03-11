package com.rag.service;

import com.rag.model.DocumentChunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetrievalService {

    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;

    @Value("${rag.top-k}")
    private int topK;

    public RetrievalService(EmbeddingService embeddingService, VectorStore vectorStore) {
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
    }

    /**
     * Embed the user query and retrieve top-K similar chunks from the vector store.
     */
    public List<DocumentChunk> retrieve(String query) {
        double[] queryEmbedding = embeddingService.embedText(query);
        return vectorStore.searchSimilar(queryEmbedding, topK);
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public int getTopK() { return topK; }
}
