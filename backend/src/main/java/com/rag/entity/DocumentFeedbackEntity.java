package com.rag.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.OffsetDateTime;

@TableName("document_feedback")
public class DocumentFeedbackEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String traceId;

    private String userId;

    private String feedbackType;

    private String feedbackText;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    public DocumentFeedbackEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }
    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
