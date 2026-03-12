package com.rag.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rag.common.ApiResponse;
import com.rag.common.BizException;
import com.rag.entity.UserSettingsEntity;
import com.rag.mapper.UserSettingsMapper;
import com.rag.security.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final UserSettingsMapper userSettingsMapper;
    private final SecurityUtil securityUtil;

    public SettingsController(UserSettingsMapper userSettingsMapper, SecurityUtil securityUtil) {
        this.userSettingsMapper = userSettingsMapper;
        this.securityUtil = securityUtil;
    }

    public static class UpdateSettingsRequest {
        @NotBlank(message = "Embedding model is required")
        private String embeddingModel;
        @NotBlank(message = "Rerank model is required")
        private String rerankModel;
        @NotBlank(message = "Chat model is required")
        private String chatModel;
        @NotNull(message = "Chunk size is required")
        private Integer chunkSize;
        @NotNull(message = "Chunk overlap is required")
        private Integer chunkOverlap;
        @NotNull(message = "Top K is required")
        private Integer topK;
        @NotNull(message = "Top N is required")
        private Integer topN;

        public String getEmbeddingModel() { return embeddingModel; }
        public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
        public String getRerankModel() { return rerankModel; }
        public void setRerankModel(String rerankModel) { this.rerankModel = rerankModel; }
        public String getChatModel() { return chatModel; }
        public void setChatModel(String chatModel) { this.chatModel = chatModel; }
        public Integer getChunkSize() { return chunkSize; }
        public void setChunkSize(Integer chunkSize) { this.chunkSize = chunkSize; }
        public Integer getChunkOverlap() { return chunkOverlap; }
        public void setChunkOverlap(Integer chunkOverlap) { this.chunkOverlap = chunkOverlap; }
        public Integer getTopK() { return topK; }
        public void setTopK(Integer topK) { this.topK = topK; }
        public Integer getTopN() { return topN; }
        public void setTopN(Integer topN) { this.topN = topN; }
    }

    @GetMapping
    public ApiResponse<UserSettingsEntity> getSettings() {
        String userId = securityUtil.getCurrentUserId();
        UserSettingsEntity settings = userSettingsMapper.selectOne(
                new LambdaQueryWrapper<UserSettingsEntity>()
                        .eq(UserSettingsEntity::getUserId, userId)
        );

        if (settings == null) {
            // Create default settings
            settings = createDefaultSettings(userId);
        }

        return ApiResponse.ok(settings);
    }

    @PutMapping
    public ApiResponse<UserSettingsEntity> updateSettings(@Valid @RequestBody UpdateSettingsRequest request) {
        String userId = securityUtil.getCurrentUserId();
        UserSettingsEntity settings = userSettingsMapper.selectOne(
                new LambdaQueryWrapper<UserSettingsEntity>()
                        .eq(UserSettingsEntity::getUserId, userId)
        );

        if (settings == null) {
            settings = createDefaultSettings(userId);
        }

        settings.setEmbeddingModel(request.getEmbeddingModel());
        settings.setRerankModel(request.getRerankModel());
        settings.setChatModel(request.getChatModel());
        settings.setChunkSize(request.getChunkSize());
        settings.setChunkOverlap(request.getChunkOverlap());
        settings.setTopK(request.getTopK());
        settings.setTopN(request.getTopN());

        userSettingsMapper.updateById(settings);

        return ApiResponse.ok(settings);
    }

    private UserSettingsEntity createDefaultSettings(String userId) {
        UserSettingsEntity settings = new UserSettingsEntity();
        settings.setId(UUID.randomUUID().toString());
        settings.setUserId(userId);
        settings.setEmbeddingModel("Qwen/Qwen3-Embedding-0.6B");
        settings.setRerankModel("Qwen/Qwen3-Reranker-0.6B");
        settings.setChatModel("Qwen/Qwen3-8B");
        settings.setChunkSize(500);
        settings.setChunkOverlap(100);
        settings.setTopK(10);
        settings.setTopN(5);
        userSettingsMapper.insert(settings);
        return settings;
    }
}
