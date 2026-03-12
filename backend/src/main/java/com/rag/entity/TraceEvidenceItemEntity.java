package com.rag.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@TableName("trace_evidence_items")
public class TraceEvidenceItemEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String traceId;

    private String documentId;

    private String chunkId;

    private Integer retrievalRank;

    private BigDecimal retrievalScore;

    private Integer rerankRank;

    private BigDecimal rerankScore;

    private Boolean isSelected;

    private Integer citationNo;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    public TraceEvidenceItemEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getChunkId() { return chunkId; }
    public void setChunkId(String chunkId) { this.chunkId = chunkId; }
    public Integer getRetrievalRank() { return retrievalRank; }
    public void setRetrievalRank(Integer retrievalRank) { this.retrievalRank = retrievalRank; }
    public BigDecimal getRetrievalScore() { return retrievalScore; }
    public void setRetrievalScore(BigDecimal retrievalScore) { this.retrievalScore = retrievalScore; }
    public Integer getRerankRank() { return rerankRank; }
    public void setRerankRank(Integer rerankRank) { this.rerankRank = rerankRank; }
    public BigDecimal getRerankScore() { return rerankScore; }
    public void setRerankScore(BigDecimal rerankScore) { this.rerankScore = rerankScore; }
    public Boolean getIsSelected() { return isSelected; }
    public void setIsSelected(Boolean isSelected) { this.isSelected = isSelected; }
    public Integer getCitationNo() { return citationNo; }
    public void setCitationNo(Integer citationNo) { this.citationNo = citationNo; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
