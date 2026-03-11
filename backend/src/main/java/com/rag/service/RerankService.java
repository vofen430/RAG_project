package com.rag.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.config.ModelConfig;
import com.rag.config.SiliconFlowConfig;
import com.rag.model.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RerankService {

    private static final Logger log = LoggerFactory.getLogger(RerankService.class);

    private final WebClient webClient;
    private final SiliconFlowConfig siliconFlowConfig;
    private final ModelConfig modelConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${rag.top-n}")
    private int topN;

    public RerankService(WebClient siliconFlowWebClient, SiliconFlowConfig siliconFlowConfig, ModelConfig modelConfig) {
        this.webClient = siliconFlowWebClient;
        this.siliconFlowConfig = siliconFlowConfig;
        this.modelConfig = modelConfig;
    }

    /**
     * Rerank the retrieved chunks using SiliconFlow Rerank API.
     * Returns the top-N most relevant chunks after reranking.
     */
    public List<DocumentChunk> rerank(String query, List<DocumentChunk> chunks) {
        if (chunks.isEmpty()) return chunks;

        String model = modelConfig.getSelectedModel("reranking");

        try {
            List<String> documents = chunks.stream()
                    .map(DocumentChunk::getContent)
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
                List<DocumentChunk> reranked = new ArrayList<>();
                for (JsonNode result : results) {
                    int index = result.get("index").asInt();
                    if (index < chunks.size()) {
                        reranked.add(chunks.get(index));
                    }
                }
                log.info("Reranked {} chunks to {} using model {}", chunks.size(), reranked.size(), model);
                return reranked;
            }

        } catch (Exception e) {
            log.error("Reranking failed: {}. Falling back to original order.", e.getMessage());
        }

        // Fallback: return top-N without reranking
        return chunks.subList(0, Math.min(topN, chunks.size()));
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }

    public int getTopN() { return topN; }
}
