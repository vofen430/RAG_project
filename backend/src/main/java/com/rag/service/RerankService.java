package com.rag.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.config.ModelConfig;
import com.rag.config.SiliconFlowConfig;
import com.rag.entity.DocumentChunkEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reranking service. Now returns RerankResult objects with scores for trace persistence.
 */
@Service
public class RerankService {

    private static final Logger log = LoggerFactory.getLogger(RerankService.class);

    private final WebClient webClient;
    private final SiliconFlowConfig siliconFlowConfig;
    private final ModelConfig modelConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RerankService(WebClient siliconFlowWebClient, SiliconFlowConfig siliconFlowConfig, ModelConfig modelConfig) {
        this.webClient = siliconFlowWebClient;
        this.siliconFlowConfig = siliconFlowConfig;
        this.modelConfig = modelConfig;
    }

    public record RerankResult(
        DocumentChunkEntity chunk,
        int rerankRank,
        BigDecimal rerankScore
    ) {}

    /**
     * Rerank the retrieved chunks and return results with scores.
     */
    public List<RerankResult> rerank(String query, List<DocumentChunkEntity> chunks, int topN) {
        if (chunks.isEmpty()) return Collections.emptyList();

        String model = modelConfig.getSelectedModel("reranking");

        try {
            List<String> documents = chunks.stream()
                    .map(DocumentChunkEntity::getContentText)
                    .collect(Collectors.toList());

            Map<String, Object> request = new LinkedHashMap<>();
            request.put("model", model);
            request.put("query", query);
            request.put("documents", documents);
            request.put("top_n", topN);
            request.put("return_documents", false);

            String responseBody = webClient.post()
                    .uri("/rerank")
                    .header("Authorization", "Bearer " + siliconFlowConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.get("results");

            if (results != null && results.isArray()) {
                List<RerankResult> reranked = new ArrayList<>();
                int rank = 1;
                for (JsonNode result : results) {
                    int index = result.get("index").asInt();
                    double score = result.has("relevance_score")
                            ? result.get("relevance_score").asDouble()
                            : 0.0;
                    if (index < chunks.size()) {
                        reranked.add(new RerankResult(
                                chunks.get(index),
                                rank++,
                                BigDecimal.valueOf(score)
                        ));
                    }
                }
                log.info("Reranked {} chunks to {} using model {}", chunks.size(), reranked.size(), model);
                return reranked;
            }

        } catch (Exception e) {
            log.error("Reranking failed: {}. Falling back to original order.", e.getMessage());
        }

        // Fallback: return top-N without reranking
        List<RerankResult> fallback = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, chunks.size()); i++) {
            fallback.add(new RerankResult(chunks.get(i), i + 1, BigDecimal.ZERO));
        }
        return fallback;
    }
}
