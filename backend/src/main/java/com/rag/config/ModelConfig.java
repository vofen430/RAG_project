package com.rag.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ModelConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode modelsRoot;
    private final Map<String, String> selectedModels = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws IOException {
        ClassPathResource resource = new ClassPathResource("models.json");
        try (InputStream is = resource.getInputStream()) {
            modelsRoot = objectMapper.readTree(is);
        }
        // Load default selections
        modelsRoot.fieldNames().forEachRemaining(stage -> {
            JsonNode stageNode = modelsRoot.get(stage);
            if (stageNode.has("selected")) {
                selectedModels.put(stage, stageNode.get("selected").asText());
            }
        });
    }

    public String getSelectedModel(String stage) {
        return selectedModels.getOrDefault(stage, "");
    }

    public void setSelectedModel(String stage, String modelId) {
        selectedModels.put(stage, modelId);
    }

    public Map<String, Object> getAllModelsConfig() {
        Map<String, Object> result = new LinkedHashMap<>();
        modelsRoot.fieldNames().forEachRemaining(stage -> {
            Map<String, Object> stageConfig = new LinkedHashMap<>();
            stageConfig.put("selected", selectedModels.getOrDefault(stage, ""));
            List<Map<String, Object>> options = new ArrayList<>();
            JsonNode optionsNode = modelsRoot.get(stage).get("options");
            if (optionsNode != null && optionsNode.isArray()) {
                for (JsonNode opt : optionsNode) {
                    Map<String, Object> option = new LinkedHashMap<>();
                    opt.fieldNames().forEachRemaining(f -> option.put(f, opt.get(f).isNumber() ? opt.get(f).numberValue() : opt.get(f).asText()));
                    options.add(option);
                }
            }
            stageConfig.put("options", options);
            result.put(stage, stageConfig);
        });
        return result;
    }

    public Map<String, String> getSelectedModels() {
        return new LinkedHashMap<>(selectedModels);
    }
}
