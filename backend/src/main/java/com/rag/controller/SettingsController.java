package com.rag.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rag.common.ApiResponse;
import com.rag.config.ModelConfig;
import com.rag.config.SiliconFlowConfig;
import com.rag.entity.UserSettingsEntity;
import com.rag.mapper.UserSettingsMapper;
import com.rag.security.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final UserSettingsMapper userSettingsMapper;
    private final SecurityUtil securityUtil;
    private final ModelConfig modelConfig;
    private final SiliconFlowConfig siliconFlowConfig;

    public SettingsController(UserSettingsMapper userSettingsMapper, SecurityUtil securityUtil,
                              ModelConfig modelConfig, SiliconFlowConfig siliconFlowConfig) {
        this.userSettingsMapper = userSettingsMapper;
        this.securityUtil = securityUtil;
        this.modelConfig = modelConfig;
        this.siliconFlowConfig = siliconFlowConfig;
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
        private String apiKey;

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
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }

    /**
     * Returns available model options from the SiliconFlow API catalog (models.json).
     */
    @GetMapping("/models")
    public ApiResponse<Map<String, Object>> getModelOptions() {
        return ApiResponse.ok(modelConfig.getAllModelsConfig());
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getSettings() {
        String userId = securityUtil.getCurrentUserId();
        UserSettingsEntity settings = userSettingsMapper.selectOne(
                new LambdaQueryWrapper<UserSettingsEntity>()
                        .eq(UserSettingsEntity::getUserId, userId)
        );

        if (settings == null) {
            settings = createDefaultSettings(userId);
        }

        // Build response with masked API key (entity uses @JsonIgnore on apiKey)
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", settings.getId());
        response.put("userId", settings.getUserId());
        response.put("embeddingModel", settings.getEmbeddingModel());
        response.put("rerankModel", settings.getRerankModel());
        response.put("chatModel", settings.getChatModel());
        response.put("chunkSize", settings.getChunkSize());
        response.put("chunkOverlap", settings.getChunkOverlap());
        response.put("topK", settings.getTopK());
        response.put("topN", settings.getTopN());
        response.put("apiKeyMasked", maskApiKey(siliconFlowConfig.getApiKey()));
        response.put("hasApiKey", siliconFlowConfig.getApiKey() != null
                && !siliconFlowConfig.getApiKey().isEmpty()
                && !"your-api-key-here".equals(siliconFlowConfig.getApiKey()));
        response.put("createdAt", settings.getCreatedAt());
        response.put("updatedAt", settings.getUpdatedAt());

        return ApiResponse.ok(response);
    }

    @PutMapping
    public ApiResponse<Map<String, Object>> updateSettings(@Valid @RequestBody UpdateSettingsRequest request) {
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

        // Handle API key update: only update if provided and not the masked placeholder
        if (request.getApiKey() != null && !request.getApiKey().isBlank()
                && !request.getApiKey().contains("••••")) {
            settings.setApiKey(request.getApiKey());
            // Also update the runtime config so it takes effect immediately
            siliconFlowConfig.setApiKey(request.getApiKey());
        }

        userSettingsMapper.updateById(settings);

        // Return same format as GET
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", settings.getId());
        response.put("userId", settings.getUserId());
        response.put("embeddingModel", settings.getEmbeddingModel());
        response.put("rerankModel", settings.getRerankModel());
        response.put("chatModel", settings.getChatModel());
        response.put("chunkSize", settings.getChunkSize());
        response.put("chunkOverlap", settings.getChunkOverlap());
        response.put("topK", settings.getTopK());
        response.put("topN", settings.getTopN());
        response.put("apiKeyMasked", maskApiKey(siliconFlowConfig.getApiKey()));
        response.put("hasApiKey", true);
        response.put("createdAt", settings.getCreatedAt());
        response.put("updatedAt", settings.getUpdatedAt());

        return ApiResponse.ok(response);
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
        settings.setApiKey("");
        userSettingsMapper.insert(settings);
        return settings;
    }

    /**
     * Mask an API key for display: show first 3 and last 4 chars.
     * e.g. "sk-abcdefghijklmnop" -> "sk-••••••••••mnop"
     */
    private String maskApiKey(String key) {
        if (key == null || key.length() <= 8 || "your-api-key-here".equals(key)) {
            return "";
        }
        return key.substring(0, 3) + "••••••••" + key.substring(key.length() - 4);
    }
}
