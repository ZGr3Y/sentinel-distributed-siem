package com.sentinel.api.security;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Pattern: RateLimiter Gateway (L5_Auth-hash, slides 14-16)
 *
 * Prof. Tramontana's security pipeline:
 * GUI → RATE-LIMITER → AUTHENTICATOR → AUDIT → ACCESS CONTROL → SERVICE
 *
 * This filter sits BEFORE the JWT Authentication filter.
 * Uses Google Guava RateLimiter as shown in the slides:
 * RateLimiter.create(limitPerSecond), rateLimiter.tryAcquire()
 *
 * If too many requests arrive, the caller receives HTTP 429 Too Many Requests.
 */
@Component
@Order(1)
public class RateLimiterFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterFilter.class);

    // Allow 50 requests per second across all clients (adjustable)
    private final RateLimiter rateLimiter = RateLimiter.create(50.0);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // tryAcquire returns false immediately if no permits are available
        if (!rateLimiter.tryAcquire()) {
            log.warn("⚠️ Rate limit exceeded. Rejecting request.");
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.getWriter().write("{\"error\": \"Rate limit exceeded. Please slow down.\"}");
            httpResponse.setContentType("application/json");
            return;
        }

        chain.doFilter(request, response);
    }
}
