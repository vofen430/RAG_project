package com.rag.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.OffsetDateTime;

/**
 * Document chunk entity. Note: embedding_vector is handled via custom XML mapper
 * because MyBatis-Plus doesn't natively support pgvector types.
 */
@TableName("document_chunks")
public class DocumentChunkEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String documentId;

    private Integer chunkIndex;

    private String sectionLabel;

    private String contentText;

    private String contentSummary;

    private Integer startOffset;

    private Integer endOffset;

    /**
     * Stored as JSONB in PostgreSQL. Mapped as String (JSON) for simplicity.
     */
    private String entityList;

    private String embeddingModel;

    /**
     * The embedding vector is not mapped directly in the entity; it is handled
     * via custom SQL in the XML mapper to use pgvector's vector type.
     */
    @TableField(exist = false)
    private double[] embeddingVector;

    private String contentHash;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    public DocumentChunkEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }
    public String getSectionLabel() { return sectionLabel; }
    public void setSectionLabel(String sectionLabel) { this.sectionLabel = sectionLabel; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public String getContentSummary() { return contentSummary; }
    public void setContentSummary(String contentSummary) { this.contentSummary = contentSummary; }
    public Integer getStartOffset() { return startOffset; }
    public void setStartOffset(Integer startOffset) { this.startOffset = startOffset; }
    public Integer getEndOffset() { return endOffset; }
    public void setEndOffset(Integer endOffset) { this.endOffset = endOffset; }
    public String getEntityList() { return entityList; }
    public void setEntityList(String entityList) { this.entityList = entityList; }
    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
    public double[] getEmbeddingVector() { return embeddingVector; }
    public void setEmbeddingVector(double[] embeddingVector) { this.embeddingVector = embeddingVector; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
