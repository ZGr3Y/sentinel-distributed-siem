package com.sentinel.api.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;

/**
 * Pattern: Reference Monitor (L2_ReferenceMonitor)
 * Intercepts ALL requests and evaluates them against authorization rules.
 *
 * This filter extracts the JWT from the Authorization header, validates it,
 * and sets the Spring Security authentication context with the user's role.
 *
 * Pattern: RBAC (L3_RoleBasedAC)
 * The role claim from the JWT is converted into a Spring Security
 * GrantedAuthority,
 * enabling role-based access control on endpoints via SecurityConfig.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            DecodedJWT decoded = jwtUtils.validateToken(token);

            if (decoded != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String userId = decoded.getSubject();
                String role = decoded.getClaim("role").asString();

                // RBAC: Convert role to Spring Security GrantedAuthority
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
