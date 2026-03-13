package com.rag.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rag.entity.UserEntity;
import com.rag.entity.UserSettingsEntity;
import com.rag.mapper.UserMapper;
import com.rag.mapper.UserSettingsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Initializes local storage directories, verifies seed data, and loads
 * the user-saved API key from the database on startup.
 */
@Component
public class StorageInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    private final UserMapper userMapper;
    private final UserSettingsMapper userSettingsMapper;
    private final PasswordEncoder passwordEncoder;
    private final SiliconFlowConfig siliconFlowConfig;
    private final String storageRoot;

    public StorageInitializer(UserMapper userMapper, UserSettingsMapper userSettingsMapper,
                              PasswordEncoder passwordEncoder, SiliconFlowConfig siliconFlowConfig,
                              @org.springframework.beans.factory.annotation.Value("${app.storage.root}") String storageRoot) {
        this.userMapper = userMapper;
        this.userSettingsMapper = userSettingsMapper;
        this.passwordEncoder = passwordEncoder;
        this.siliconFlowConfig = siliconFlowConfig;
        this.storageRoot = storageRoot;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create local storage directories
        String[] dirs = {"documents", "tmp", "exports", "logs"};
        for (String dir : dirs) {
            Path path = Paths.get(storageRoot, dir);
            Files.createDirectories(path);
            log.info("Storage directory ensured: {}", path);
        }

        // Update seed user password hash if needed (the migration SQL has a placeholder hash)
        UserEntity demoUser = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, "demo")
        );
        if (demoUser != null) {
            // Re-hash with the current encoder to ensure consistency
            String correctHash = passwordEncoder.encode("demo-password");
            demoUser.setPasswordHash(correctHash);
            userMapper.updateById(demoUser);
            log.info("Demo user password hash updated");
        }

        // Load user-saved API key from DB (survives backend restarts)
        loadApiKeyFromDatabase();
    }

    /**
     * If a user has previously saved an API key via the Settings page,
     * load it into SiliconFlowConfig so it takes effect immediately
     * without needing to re-enter the key after each restart.
     */
    private void loadApiKeyFromDatabase() {
        try {
            List<UserSettingsEntity> allSettings = userSettingsMapper.selectList(null);
            for (UserSettingsEntity settings : allSettings) {
                String dbApiKey = settings.getApiKey();
                if (dbApiKey != null && !dbApiKey.isBlank()
                        && !dbApiKey.equals("your-api-key-here")) {
                    siliconFlowConfig.setApiKey(dbApiKey);
                    log.info("Loaded API key from user_settings (user: {})", settings.getUserId());
                    return; // Use the first valid key found
                }
            }
            log.info("No user-saved API key found in database, using environment variable");
        } catch (Exception e) {
            log.warn("Failed to load API key from database: {}", e.getMessage());
        }
    }
}

