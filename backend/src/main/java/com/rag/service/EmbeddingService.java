package com.rag.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.config.SiliconFlowConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    private static final int BATCH_SIZE = 32;

    private final WebClient webClient;
    private final SiliconFlowConfig siliconFlowConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmbeddingService(WebClient siliconFlowWebClient, SiliconFlowConfig siliconFlowConfig) {
        this.webClient = siliconFlowWebClient;
        this.siliconFlowConfig = siliconFlowConfig;
    }

    /**
     * Embed a single text string using the specified model.
     */
    public double[] embedText(String text, String model) {
        List<double[]> results = embedBatch(Collections.singletonList(text), model);
        if (results.isEmpty() || results.get(0).length == 0) {
            throw new RuntimeException("Embedding failed: API returned no vectors. Please check your API Key and model configuration.");
        }
        return results.get(0);
    }

    /**
     * Embed a batch of texts using the specified model.
     * SiliconFlow API: POST /embeddings
     * Request: { model, input (string[]), encoding_format }
     * Response: { data: [{ index, embedding: number[] }], usage }
     */
    public List<double[]> embedBatch(List<String> texts, String model) {
        List<double[]> allEmbeddings = new ArrayList<>();

        for (int i = 0; i < texts.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, texts.size());
            List<String> batch = texts.subList(i, end);

            try {
                Map<String, Object> request = new LinkedHashMap<>();
                request.put("model", model);
                request.put("input", batch);
                request.put("encoding_format", "float");

                String responseBody = webClient.post()
                        .uri("/embeddings")
                        .header("Authorization", "Bearer " + siliconFlowConfig.getApiKey())
                        .header("Content-Type", "application/json")
                        .bodyValue(objectMapper.writeValueAsString(request))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode data = root.get("data");

                if (data != null && data.isArray()) {
                    // Sort by index to ensure correct order
                    List<JsonNode> items = new ArrayList<>();
                    for (JsonNode item : data) {
                        items.add(item);
                    }
                    items.sort(Comparator.comparingInt(a -> a.get("index").asInt()));

                    for (JsonNode item : items) {
                        JsonNode embeddingNode = item.get("embedding");
                        double[] vec = new double[embeddingNode.size()];
                        for (int j = 0; j < embeddingNode.size(); j++) {
                            vec[j] = embeddingNode.get(j).asDouble();
                        }
                        allEmbeddings.add(vec);
                    }
                }

                log.info("Embedded batch {}-{} of {} texts using model {}", i, end, texts.size(), model);

            } catch (Exception e) {
                log.error("Embedding failed for batch {}-{}: {}", i, end, e.getMessage());
                throw new RuntimeException("Embedding API call failed: " + e.getMessage(), e);
            }
        }

        return allEmbeddings;
    }
}
