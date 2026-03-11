package com.rag.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.config.ModelConfig;
import com.rag.config.SiliconFlowConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final WebClient webClient;
    private final SiliconFlowConfig siliconFlowConfig;
    private final ModelConfig modelConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(WebClient siliconFlowWebClient, SiliconFlowConfig siliconFlowConfig, ModelConfig modelConfig) {
        this.webClient = siliconFlowWebClient;
        this.siliconFlowConfig = siliconFlowConfig;
        this.modelConfig = modelConfig;
    }

    /**
     * Call SiliconFlow Chat Completions API with streaming.
     * Returns a Flux of content tokens for SSE streaming.
     */
    public Flux<String> streamChat(String systemPrompt, String userMessage) {
        String model = modelConfig.getSelectedModel("chat");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", model);
        request.put("messages", messages);
        request.put("stream", true);
        request.put("max_tokens", 4096);
        request.put("temperature", 0.7);
        request.put("top_p", 0.7);
        request.put("enable_thinking", false);

        try {
            String requestBody = objectMapper.writeValueAsString(request);

            return webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + siliconFlowConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .filter(line -> !line.isBlank() && !line.equals("[DONE]"))
                    .mapNotNull(line -> {
                        try {
                            // Each line from SSE is a JSON object
                            String data = line.startsWith("data: ") ? line.substring(6) : line;
                            if (data.equals("[DONE]") || data.isBlank()) return null;

                            JsonNode node = objectMapper.readTree(data);
                            JsonNode choices = node.get("choices");
                            if (choices != null && choices.isArray() && choices.size() > 0) {
                                JsonNode delta = choices.get(0).get("delta");
                                if (delta != null && delta.has("content")) {
                                    return delta.get("content").asText();
                                }
                            }
                        } catch (Exception e) {
                            // Ignore parse errors for incomplete SSE chunks
                        }
                        return null;
                    })
                    .filter(Objects::nonNull);

        } catch (Exception e) {
            log.error("Chat streaming failed: {}", e.getMessage());
            return Flux.just("Error: " + e.getMessage());
        }
    }
}
