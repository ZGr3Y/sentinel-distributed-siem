package com.sentinel.api.controller;

import com.lambdaworks.crypto.SCryptUtil;
import com.sentinel.api.dto.LoginRequest;
import com.sentinel.api.repository.UserRepository;
import com.sentinel.api.security.JwtUtils;
import com.sentinel.common.domain.entity.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * Pattern: Authenticator (L5_Auth-hash)
 *
 * Sequence diagram from slides:
 * Client -> ServerGateway -> RateLimiter.acquire() ->
 * Authenticator.checkCredentials()
 * -> SCryptUtil.check() -> UserTable.query() -> TokenGenerator.tokenBuild()
 * -> Token.create -> TokenStore.store(t) -> return token to client
 *
 * This controller implements the Authenticator role:
 * - Receives credentials (username, password)
 * - Looks up user in DB (UserTable)
 * - Verifies password hash using SCryptUtil.check() (L5_Auth-hash slide 11)
 * - Issues JWT as ProofOfIdentity (L5_Token)
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public AuthController(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest credentials) {
        // Step 1: Query UserTable
        Optional<User> userOptional = userRepository.findByUsername(credentials.getUsername());
        if (userOptional.isEmpty()) {
            log.warn("Login attempt failed: user '{}' not found", credentials.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials."));
        }

        User user = userOptional.get();

        // Step 2: Verify password using Scrypt (L5_Auth-hash slide 11)
        // SCryptUtil.check(passwd, authenticationInfo) extracts the salt internally
        if (!SCryptUtil.check(credentials.getPassword(), user.getPasswordHash())) {
            log.warn("Login attempt failed: wrong password for user '{}'", credentials.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials."));
        }

        // Step 3: Generate JWT (ProofOfIdentity) with user ID and role
        String token = jwtUtils.generateToken(user.getId(), user.getRole());

        log.info("User '{}' authenticated successfully with role '{}'", credentials.getUsername(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", user.getId(),
                "role", user.getRole(),
                "type", "Bearer"));
    }
}
