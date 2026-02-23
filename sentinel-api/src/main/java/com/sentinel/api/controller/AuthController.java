package com.sentinel.api.controller;

import com.sentinel.api.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtils jwtUtils;

    public AuthController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login() {
        // Mock login: generates a token for a random mock user
        UUID mockUserId = UUID.randomUUID();
        String token = jwtUtils.generateToken(mockUserId);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", mockUserId.toString(),
                "type", "Bearer"));
    }
}
