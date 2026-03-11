package com.rag.model;

public class DocumentInfo {
    private String id;
    private String fileName;
    private long fileSize;
    private int totalChunks;
    private String status; // UPLOADED, CHUNKING, EMBEDDING, INDEXED, ERROR
    private String errorMessage;
    private int processedChunks;

    public DocumentInfo() {}

    public DocumentInfo(String id, String fileName, long fileSize) {
        this.id = id;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.status = "UPLOADED";
        this.totalChunks = 0;
        this.processedChunks = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public int getTotalChunks() { return totalChunks; }
    public void setTotalChunks(int totalChunks) { this.totalChunks = totalChunks; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public int getProcessedChunks() { return processedChunks; }
    public void setProcessedChunks(int processedChunks) { this.processedChunks = processedChunks; }
}
