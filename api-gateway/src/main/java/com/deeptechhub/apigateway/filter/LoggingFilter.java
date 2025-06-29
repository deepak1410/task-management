package com.deeptechhub.apigateway.filter;

import com.deeptechhub.common.CommonApplicationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * A global filter for logging incoming requests and outgoing responses in a Spring Cloud Gateway application.
 * <p>
 * This filter logs request details (method, URI, headers) before processing and response details (status, duration, headers)
 * after processing. It also manages a correlation ID for request tracing and clears MDC (Mapped Diagnostic Context) after each request.
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    /**
     * Filters incoming requests and outgoing responses, logging relevant details for each.
     *
     * @param exchange the current server web exchange
     * @param chain provides a way to delegate to the next filter
     * @return a Mono<Void> indicating completion or error
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        final String requestId = getCorrelationId(exchange, request);
        final long startTime = System.currentTimeMillis();

        MDC.put(CommonApplicationConstants.CORRELATION_ID_HEADER, requestId);
        log.info("[{}] Incoming Request: {} {} | Headers: {}", requestId, request.getMethod(), request.getURI(), request.getHeaders());

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    ServerHttpResponse response = exchange.getResponse();
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("[{}] Response: {} | Duration: {} ms | Headers: {}",
                            requestId, response.getStatusCode(), duration, response.getHeaders());
                    MDC.clear();
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("[{}] ERROR occurred after {} ms: {}", requestId, duration, error.getMessage(), error);
                    MDC.clear();
                });
    }

    @Override
    public int getOrder() {
        return -1; // Highest priority to run first
    }

    private String getCorrelationId(final ServerWebExchange exchange, final ServerHttpRequest request) {
        String correlationId = request.getHeaders().getFirst(CommonApplicationConstants.CORRELATION_ID_HEADER);
        if(correlationId == null) {
            correlationId = UUID.randomUUID().toString();
            exchange.getRequest().mutate().header(CommonApplicationConstants.CORRELATION_ID_HEADER, correlationId).build();
        }
        return correlationId;
    }
}
