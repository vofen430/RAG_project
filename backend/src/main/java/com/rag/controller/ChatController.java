package com.rag.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.common.ApiResponse;
import com.rag.common.BizException;
import com.rag.entity.*;
import com.rag.mapper.*;
import com.rag.security.SecurityUtil;
import com.rag.service.RagPipelineService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final RagPipelineService ragPipelineService;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final RagQueryTraceMapper traceMapper;
    private final TraceEvidenceItemMapper evidenceItemMapper;
    private final DocumentFeedbackMapper feedbackMapper;
    private final DocumentMapper documentMapper;
    private final DocumentChunkMapper documentChunkMapper;
    private final SecurityUtil securityUtil;
    private final ObjectMapper objectMapper;

    public ChatController(RagPipelineService ragPipelineService,
                          ChatSessionMapper chatSessionMapper,
                          ChatMessageMapper chatMessageMapper,
                          RagQueryTraceMapper traceMapper,
                          TraceEvidenceItemMapper evidenceItemMapper,
                          DocumentFeedbackMapper feedbackMapper,
                          DocumentMapper documentMapper,
                          DocumentChunkMapper documentChunkMapper,
                          SecurityUtil securityUtil,
                          ObjectMapper objectMapper) {
        this.ragPipelineService = ragPipelineService;
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.traceMapper = traceMapper;
        this.evidenceItemMapper = evidenceItemMapper;
        this.feedbackMapper = feedbackMapper;
        this.documentMapper = documentMapper;
        this.documentChunkMapper = documentChunkMapper;
        this.securityUtil = securityUtil;
        this.objectMapper = objectMapper;
    }

    // --- Request DTOs ---

    public static class CreateSessionRequest {
        @NotBlank(message = "Title is required")
        private String title;
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

    public static class StreamQueryRequest {
        @NotBlank(message = "Query is required")
        private String query;
        private List<String> documentIds;
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public List<String> getDocumentIds() { return documentIds; }
        public void setDocumentIds(List<String> documentIds) { this.documentIds = documentIds; }
    }

    public static class FeedbackRequest {
        @NotBlank(message = "Feedback type is required")
        private String feedbackType;
        private String feedbackText;
        public String getFeedbackType() { return feedbackType; }
        public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }
        public String getFeedbackText() { return feedbackText; }
        public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }
    }

    // --- Session APIs ---

    @PostMapping("/sessions")
    public ApiResponse<ChatSessionEntity> createSession(@Valid @RequestBody CreateSessionRequest request) {
        String userId = securityUtil.getCurrentUserId();

        ChatSessionEntity session = new ChatSessionEntity();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setTitle(request.getTitle());
        session.setSessionStatus("ACTIVE");
        chatSessionMapper.insert(session);

        log.info("Chat session created: {}", session.getId());
        return ApiResponse.ok(session);
    }

    @GetMapping("/sessions")
    public ApiResponse<List<ChatSessionEntity>> listSessions() {
        String userId = securityUtil.getCurrentUserId();
        List<ChatSessionEntity> sessions = chatSessionMapper.selectList(
                new LambdaQueryWrapper<ChatSessionEntity>()
                        .eq(ChatSessionEntity::getUserId, userId)
                        .orderByDesc(ChatSessionEntity::getCreatedAt)
        );
        return ApiResponse.ok(sessions);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<Page<ChatMessageEntity>> getMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        verifySessionOwnership(sessionId);

        Page<ChatMessageEntity> pageObj = new Page<>(page, size);
        chatMessageMapper.selectPage(pageObj,
                new LambdaQueryWrapper<ChatMessageEntity>()
                        .eq(ChatMessageEntity::getSessionId, sessionId)
                        .orderByAsc(ChatMessageEntity::getMessageIndex)
        );
        return ApiResponse.ok(pageObj);
    }

    // --- Streaming Query API ---

    @PostMapping(value = "/sessions/{sessionId}/query/stream",
                 produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamQuery(@PathVariable String sessionId,
                                    @Valid @RequestBody StreamQueryRequest request) {
        verifySessionOwnership(sessionId);
        String userId = securityUtil.getCurrentUserId();

        // Save user message
        int nextIndex = getNextMessageIndex(sessionId);
        ChatMessageEntity userMsg = new ChatMessageEntity();
        userMsg.setId(UUID.randomUUID().toString());
        userMsg.setSessionId(sessionId);
        userMsg.setMessageRole("user");
        userMsg.setMessageIndex(nextIndex);
        userMsg.setContentText(request.getQuery());
        chatMessageMapper.insert(userMsg);

        // Run query pipeline
        RagPipelineService.QueryResult result = ragPipelineService.query(
                sessionId, request.getQuery(), request.getDocumentIds(), userId);

        String traceId = result.traceId();
        StringBuilder fullAnswer = new StringBuilder();

        return result.stream()
                .map(token -> {
                    fullAnswer.append(token);
                    try {
                        Map<String, String> payload = Map.of("traceId", traceId, "content", token);
                        return "event: token\ndata: " + objectMapper.writeValueAsString(payload) + "\n\n";
                    } catch (Exception e) {
                        return "event: token\ndata: " + token + "\n\n";
                    }
                })
                .concatWith(Flux.defer(() -> {
                    // Save assistant message
                    int assistantIndex = getNextMessageIndex(sessionId);
                    ChatMessageEntity assistantMsg = new ChatMessageEntity();
                    assistantMsg.setId(UUID.randomUUID().toString());
                    assistantMsg.setSessionId(sessionId);
                    assistantMsg.setMessageRole("assistant");
                    assistantMsg.setMessageIndex(assistantIndex);
                    assistantMsg.setContentText(fullAnswer.toString());
                    assistantMsg.setTraceId(traceId);
                    chatMessageMapper.insert(assistantMsg);

                    // Count citations for the complete event
                    long citationCount = evidenceItemMapper.selectCount(
                            new LambdaQueryWrapper<TraceEvidenceItemEntity>()
                                    .eq(TraceEvidenceItemEntity::getTraceId, traceId)
                                    .eq(TraceEvidenceItemEntity::getIsSelected, true)
                    );

                    try {
                        Map<String, Object> payload = new LinkedHashMap<>();
                        payload.put("traceId", traceId);
                        payload.put("messageId", assistantMsg.getId());
                        payload.put("citationCount", citationCount);
                        payload.put("finishedAt", OffsetDateTime.now().toString());
                        return Flux.just("event: complete\ndata: " + objectMapper.writeValueAsString(payload) + "\n\n");
                    } catch (Exception e) {
                        return Flux.just("event: complete\ndata: {}\n\n");
                    }
                }))
                .onErrorResume(e -> {
                    log.error("Streaming error: {}", e.getMessage());
                    try {
                        Map<String, String> errPayload = Map.of("traceId", traceId, "error", e.getMessage());
                        return Flux.just("event: error\ndata: " + objectMapper.writeValueAsString(errPayload) + "\n\n");
                    } catch (Exception ex) {
                        return Flux.just("event: error\ndata: {\"error\":\"" + e.getMessage() + "\"}\n\n");
                    }
                });
    }

    // --- Trace APIs ---

    @GetMapping("/traces/{traceId}")
    public ApiResponse<Map<String, Object>> getTrace(@PathVariable String traceId) {
        RagQueryTraceEntity trace = traceMapper.selectById(traceId);
        if (trace == null) {
            throw new BizException("NOT_FOUND", "Trace not found");
        }

        String userId = securityUtil.getCurrentUserId();
        if (!trace.getUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "Trace does not belong to current user");
        }

        // Get evidence items
        List<TraceEvidenceItemEntity> evidenceItems = evidenceItemMapper.selectList(
                new LambdaQueryWrapper<TraceEvidenceItemEntity>()
                        .eq(TraceEvidenceItemEntity::getTraceId, traceId)
                        .eq(TraceEvidenceItemEntity::getIsSelected, true)
                        .orderByAsc(TraceEvidenceItemEntity::getCitationNo)
        );

        // Build evidence item DTOs with document name and chunk details
        List<Map<String, Object>> evidenceList = new ArrayList<>();
        for (TraceEvidenceItemEntity item : evidenceItems) {
            Map<String, Object> ei = new LinkedHashMap<>();
            ei.put("citationNo", item.getCitationNo());
            ei.put("documentId", item.getDocumentId());

            DocumentEntity doc = documentMapper.selectById(item.getDocumentId());
            ei.put("documentName", doc != null ? doc.getFileName() : "Unknown");

            ei.put("chunkId", item.getChunkId());

            // Get chunk details
            DocumentChunkEntity chunk = documentChunkMapper.selectById(item.getChunkId());
            if (chunk != null) {
                ei.put("chunkIndex", chunk.getChunkIndex());
                ei.put("sectionLabel", chunk.getSectionLabel());
                ei.put("contentText", chunk.getContentText());
            }

            ei.put("retrievalScore", item.getRetrievalScore());
            ei.put("rerankScore", item.getRerankScore());

            evidenceList.add(ei);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("traceId", trace.getId());
        data.put("userQuery", trace.getUserQuery());
        data.put("rewrittenQuery", trace.getRewrittenQuery());
        data.put("answerText", trace.getAnswerText());
        data.put("latencyMs", trace.getLatencyMs());
        data.put("evidenceItems", evidenceList);

        return ApiResponse.ok(data);
    }

    // --- Feedback API ---

    @PostMapping("/traces/{traceId}/feedback")
    public ApiResponse<Void> submitFeedback(@PathVariable String traceId,
                                             @Valid @RequestBody FeedbackRequest request) {
        String userId = securityUtil.getCurrentUserId();

        RagQueryTraceEntity trace = traceMapper.selectById(traceId);
        if (trace == null) {
            throw new BizException("NOT_FOUND", "Trace not found");
        }

        DocumentFeedbackEntity feedback = new DocumentFeedbackEntity();
        feedback.setId(UUID.randomUUID().toString());
        feedback.setTraceId(traceId);
        feedback.setUserId(userId);
        feedback.setFeedbackType(request.getFeedbackType());
        feedback.setFeedbackText(request.getFeedbackText());
        feedbackMapper.insert(feedback);

        return ApiResponse.ok("Feedback submitted", null);
    }

    // --- Helpers ---

    private void verifySessionOwnership(String sessionId) {
        ChatSessionEntity session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BizException("NOT_FOUND", "Chat session not found");
        }
        String userId = securityUtil.getCurrentUserId();
        if (!session.getUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "Chat session does not belong to current user");
        }
    }

    private int getNextMessageIndex(String sessionId) {
        Long count = chatMessageMapper.selectCount(
                new LambdaQueryWrapper<ChatMessageEntity>()
                        .eq(ChatMessageEntity::getSessionId, sessionId)
        );
        return count != null ? count.intValue() : 0;
    }
}
