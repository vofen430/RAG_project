package com.rag.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rag.common.ApiResponse;
import com.rag.common.BizException;
import com.rag.entity.UserEntity;
import com.rag.mapper.UserMapper;
import com.rag.security.JwtTokenProvider;
import com.rag.security.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityUtil securityUtil;

    public AuthController(UserMapper userMapper, PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider, SecurityUtil securityUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.securityUtil = securityUtil;
    }

    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;
        @NotBlank(message = "Password is required")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        UserEntity user = userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getUsername, request.getUsername())
        );

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed for user: {}", request.getUsername());
            throw new BizException("AUTH_FAILED", "Invalid username or password");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BizException("ACCOUNT_INACTIVE", "User account is not active");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRoleCode());
        long expiresIn = jwtTokenProvider.getExpirationSeconds();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accessToken", token);
        data.put("tokenType", "Bearer");
        data.put("expiresIn", expiresIn);

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("displayName", user.getDisplayName());
        userInfo.put("roleCode", user.getRoleCode());
        data.put("user", userInfo);

        log.info("Login succeeded for user: {}", request.getUsername());
        return ApiResponse.ok("Login succeeded", data);
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> currentUser() {
        String userId = securityUtil.getCurrentUserId();
        UserEntity user = userMapper.selectById(userId);

        if (user == null) {
            throw new BizException("USER_NOT_FOUND", "Current user not found");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("displayName", user.getDisplayName());
        data.put("roleCode", user.getRoleCode());

        return ApiResponse.ok(data);
    }
}
