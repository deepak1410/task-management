package com.deeptechhub.apigateway.filter;

import com.deeptechhub.apigateway.client.IdentityServiceClient;
import com.deeptechhub.apigateway.config.JwtProperties;
import com.deeptechhub.apigateway.dto.JwtAuthenticationToken;
import com.deeptechhub.common.dto.UserDto;
import com.deeptechhub.common.security.JwtService;
import com.deeptechhub.common.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * WebFilter for JWT-based authentication.
 * Validates tokens, checks blacklist, and enforces authentication for non-whitelisted paths.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final IdentityServiceClient identityServiceClient;
    private final JwtProperties jwtProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Filters requests, skipping JWT authentication for whitelisted paths.
     * Validates tokens and propagates user context for authenticated requests.
     *
     * @param exchange the current server web exchange
     * @param chain the filter chain
     * @return a Mono<Void> indicating completion or error
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        // Skip authentication for whitelisted paths
        if (shouldSkipAuthentication(path)) {
            return chain.filter(exchange);
        }

        return processAuthentication(exchange, chain);
    }

    private boolean shouldSkipAuthentication(String path) {
        return jwtProperties.getExcludePaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> processAuthentication(ServerWebExchange exchange, WebFilterChain chain) {
        String token = extractToken(exchange.getRequest());

        if (token == null) {
            return unauthorized(exchange, "Missing authorization token");
        }

        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            return unauthorized(exchange, "Token revoked");
        }

        return validateTokenAndProceed(exchange, chain, token);
    }

    private Mono<Void> validateTokenAndProceed(ServerWebExchange exchange, WebFilterChain chain, String token) {
        try {
            String username = jwtService.extractUsername(token);
            if (username == null) {
                return unauthorized(exchange, "Invalid token");
            }

            return identityServiceClient.getUserByUsername(username, token)
                    .flatMap(user -> processValidToken(exchange, chain, token, user))
                    .onErrorResume(e -> handleAuthenticationError(exchange, e));
        } catch (Exception e) {
            return unauthorized(exchange, "Token validation failed");
        }
    }

    private Mono<Void> processValidToken(ServerWebExchange exchange, WebFilterChain chain, String token, UserDto user) {
        if (!jwtService.isTokenValid(token, user.getUsername())) {
            return unauthorized(exchange, "Invalid token for user");
        }

        // Create authentication object
        JwtAuthenticationToken auth = new JwtAuthenticationToken(token, user);

        // Mutate request with headers
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", user.getId().toString())
                .header("X-User-Email", user.getEmail())
                .header("X-User-Roles", user.getRole().name())
                .build();

        // Continue with the authenticated context
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7)
                : null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Auth-Error", message);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> handleAuthenticationError(ServerWebExchange exchange, Throwable e) {
        log.error("Authentication error", e);
        return unauthorized(exchange, "Authentication service unavailable");
    }
}
