package com.rag.config;

import com.rag.entity.UserSettingsEntity;
import com.rag.mapper.UserSettingsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import java.util.List;

@Configuration
public class SiliconFlowConfig {

    @Value("${siliconflow.api-key}")
    private String apiKey;

    @Value("${siliconflow.base-url}")
    private String baseUrl;

    /** Fallback key from env var, used when DB has no user-saved key */
    private String envApiKey;

    @Autowired(required = false)
    private UserSettingsMapper userSettingsMapper;

    @Bean
    public WebClient siliconFlowWebClient() {
        this.envApiKey = apiKey;

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .build();
    }

    /**
     * Get the API key: DB first (user-saved), then env var fallback.
     * This ensures keys saved via the Settings page survive backend restarts.
     */
    public String getApiKey() {
        if (userSettingsMapper != null) {
            try {
                List<UserSettingsEntity> allSettings = userSettingsMapper.selectList(null);
                for (UserSettingsEntity s : allSettings) {
                    String dbKey = s.getApiKey();
                    if (dbKey != null && !dbKey.isBlank()
                            && !"your-api-key-here".equals(dbKey)) {
                        return dbKey;
                    }
                }
            } catch (Exception ignored) {
                // DB not ready or query failed, fall back to env var
            }
        }
        return envApiKey;
    }

    /**
     * Update the fallback key (called by SettingsController on save).
     * The primary read path is now via DB, but this keeps the in-memory
     * fallback in sync for the current session.
     */
    public void setApiKey(String newKey) {
        this.envApiKey = newKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
