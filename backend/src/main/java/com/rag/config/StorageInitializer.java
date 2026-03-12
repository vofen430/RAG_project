package com.rag.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rag.entity.UserEntity;
import com.rag.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Initializes local storage directories and verifies seed data on startup.
 */
@Component
public class StorageInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final String storageRoot;

    public StorageInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder,
                              @org.springframework.beans.factory.annotation.Value("${app.storage.root}") String storageRoot) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
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
    }
}
