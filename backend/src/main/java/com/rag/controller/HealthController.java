package com.rag.controller;

import com.rag.common.ApiResponse;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    public HealthController(DataSource dataSource, RedisConnectionFactory redisConnectionFactory) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @GetMapping
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> data = new LinkedHashMap<>();

        // Check database
        try (Connection conn = dataSource.getConnection()) {
            conn.isValid(3);
            data.put("database", "UP");
        } catch (Exception e) {
            data.put("database", "DOWN");
        }

        // Check Redis
        try {
            redisConnectionFactory.getConnection().ping();
            data.put("redis", "UP");
        } catch (Exception e) {
            data.put("redis", "DOWN");
        }

        boolean allUp = data.values().stream().allMatch("UP"::equals);
        data.put("status", allUp ? "UP" : "DEGRADED");

        return ApiResponse.ok("Healthy", data);
    }
}
