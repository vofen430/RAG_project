package com.rag.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.config.ModelConfig;
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
    private final ModelConfig modelConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmbeddingService(WebClient siliconFlowWebClient, SiliconFlowConfig siliconFlowConfig, ModelConfig modelConfig) {
        this.webClient = siliconFlowWebClient;
        this.siliconFlowConfig = siliconFlowConfig;
        this.modelConfig = modelConfig;
    }

    /**
     * Embed a single text string. Returns the embedding vector.
     */
    public double[] embedText(String text) {
        List<double[]> results = embedBatch(Collections.singletonList(text));
        return results.isEmpty() ? new double[0] : results.get(0);
    }

    /**
     * Embed a batch of texts. Returns list of embedding vectors in same order.
     */
    public List<double[]> embedBatch(List<String> texts) {
        List<double[]> allEmbeddings = new ArrayList<>();
        String model = modelConfig.getSelectedModel("embedding");

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
                // Fill with empty vectors for failed batch
                for (int j = 0; j < batch.size(); j++) {
                    allEmbeddings.add(new double[0]);
                }
            }
        }

        return allEmbeddings;
    }
}
