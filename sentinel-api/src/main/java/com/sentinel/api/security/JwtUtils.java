package com.sentinel.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Date;

/**
 * Pattern: Token / JWT (L5_Token)
 *
 * Token generation and verification following Prof. Tramontana's slides
 * (L5_Token slides 9-10):
 *
 * Creation:
 * JWT.create()
 * .withIssuer("sentinel-siem") // iss claim (slide 6)
 * .withSubject(userId) // sub claim - user identifier
 * .withIssuedAt(new Date()) // iat claim
 * .withExpiresAt(...) // exp claim
 * .withClaim("role", role) // RBAC role (L3_RoleBasedAC)
 * .sign(algorithm);
 *
 * Verification:
 * JWT.require(algorithm)
 * .withIssuer("sentinel-siem") // verify issuer (slide 10)
 * .build()
 * .verify(token);
 */
@Component
public class JwtUtils {

    private static final String ISSUER = "sentinel-siem";

    @Value("${jwt.secret}")
    private String secret;

    private Algorithm algorithm;

    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(secret);
    }

    /**
     * Generates a JWT containing userId (sub), role claim, and issuer.
     */
    public String generateToken(String userId, String role) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .sign(algorithm);
    }

    /**
     * Validates a JWT and returns the decoded token.
     * Verifies signature, expiration, AND issuer (as shown in L5_Token slide 10).
     */
    public DecodedJWT validateToken(String token) {
        try {
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);
        } catch (JWTVerificationException exception) {
            return null;
        }
    }
}
