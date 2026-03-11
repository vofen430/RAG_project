package com.rag.controller;

import com.rag.config.ModelConfig;
import com.rag.config.SiliconFlowConfig;
import com.rag.service.ChunkingService;
import com.rag.service.RerankService;
import com.rag.service.RetrievalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final ModelConfig modelConfig;
    private final SiliconFlowConfig siliconFlowConfig;
    private final ChunkingService chunkingService;
    private final RetrievalService retrievalService;
    private final RerankService rerankService;

    public SettingsController(ModelConfig modelConfig, SiliconFlowConfig siliconFlowConfig,
                              ChunkingService chunkingService, RetrievalService retrievalService,
                              RerankService rerankService) {
        this.modelConfig = modelConfig;
        this.siliconFlowConfig = siliconFlowConfig;
        this.chunkingService = chunkingService;
        this.retrievalService = retrievalService;
        this.rerankService = rerankService;
    }

    @GetMapping("/models")
    public ResponseEntity<?> getModels() {
        return ResponseEntity.ok(modelConfig.getAllModelsConfig());
    }

    @PutMapping("/models")
    public ResponseEntity<?> updateModels(@RequestBody Map<String, String> selections) {
        selections.forEach(modelConfig::setSelectedModel);
        return ResponseEntity.ok(Map.of("message", "Models updated", "selected", modelConfig.getSelectedModels()));
    }

    @PutMapping("/api-key")
    public ResponseEntity<?> updateApiKey(@RequestBody Map<String, String> body) {
        String key = body.get("apiKey");
        if (key == null || key.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "API key is required"));
        }
        siliconFlowConfig.setApiKey(key);
        return ResponseEntity.ok(Map.of("message", "API key updated"));
    }

    @GetMapping("/parameters")
    public ResponseEntity<?> getParameters() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("chunkSize", chunkingService.getChunkSize());
        params.put("chunkOverlap", chunkingService.getChunkOverlap());
        params.put("topK", retrievalService.getTopK());
        params.put("topN", rerankService.getTopN());
        return ResponseEntity.ok(params);
    }

    @PutMapping("/parameters")
    public ResponseEntity<?> updateParameters(@RequestBody Map<String, Integer> params) {
        if (params.containsKey("chunkSize")) chunkingService.setChunkSize(params.get("chunkSize"));
        if (params.containsKey("chunkOverlap")) chunkingService.setChunkOverlap(params.get("chunkOverlap"));
        if (params.containsKey("topK")) retrievalService.setTopK(params.get("topK"));
        if (params.containsKey("topN")) rerankService.setTopN(params.get("topN"));
        return ResponseEntity.ok(Map.of("message", "Parameters updated"));
    }
}
