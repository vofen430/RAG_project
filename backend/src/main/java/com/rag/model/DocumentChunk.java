package com.rag.model;

import java.util.List;
import java.util.Map;

public class DocumentChunk {
    private String chunkId;
    private String documentId;
    private String content;
    private int chunkIndex;
    private Map<String, String> metadata;
    private double[] embedding;

    public DocumentChunk() {}

    public DocumentChunk(String chunkId, String documentId, String content, int chunkIndex, Map<String, String> metadata) {
        this.chunkId = chunkId;
        this.documentId = documentId;
        this.content = content;
        this.chunkIndex = chunkIndex;
        this.metadata = metadata;
    }

    public String getChunkId() { return chunkId; }
    public void setChunkId(String chunkId) { this.chunkId = chunkId; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    public double[] getEmbedding() { return embedding; }
    public void setEmbedding(double[] embedding) { this.embedding = embedding; }
}
