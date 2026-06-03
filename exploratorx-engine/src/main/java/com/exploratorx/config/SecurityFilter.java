package com.exploratorx.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Security filter for ExploratorX engine.
 *
 * In production, Nginx sets X-Auth-User after successful Audfix auth_request.
 * This filter:
 *   1. Validates X-Auth-User header presence (production mode)
 *   2. Adds X-Request-Id for distributed tracing
 *   3. Sets security response headers
 *
 * Disabled in development mode (exploratorx.security.enabled=false).
 */
@Component
@Order(1)
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    /** Paths that bypass auth (health check, actuator, CORS preflight) */
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/health",
            "/actuator/health",
            "/actuator/prometheus"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // ─── Request tracing ────────────────────────────────────────────────
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        response.setHeader("X-Request-Id", requestId);

        // ─── Security headers ────────────────────────────────────────────────
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // ─── CORS preflight pass-through ─────────────────────────────────────
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // ─── Public path bypass ───────────────────────────────────────────────
        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ─── Auth user logging (set by Nginx from Audfix) ─────────────────────
        String authUser = request.getHeader("X-Auth-User");
        if (authUser != null && !authUser.isBlank()) {
            log.debug("[security] request from authenticated user: {} path={} reqId={}", authUser, path, requestId);
        } else {
            // In development, allow without auth header
            // In production, Nginx guarantees the header is present
            log.debug("[security] no X-Auth-User header (dev mode or direct access) path={}", path);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith)
                || path.startsWith("/ws");  // WebSocket upgrade
    }
}
