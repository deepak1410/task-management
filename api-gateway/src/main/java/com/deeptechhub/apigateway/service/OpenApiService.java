package com.deeptechhub.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Service for fetching and caching OpenAPI specifications from microservices.
 */
@Service
@RequiredArgsConstructor
public class OpenApiService {
    private static final Logger log = LoggerFactory.getLogger(OpenApiService.class);
    private final WebClient webClient;
    private final ReactiveRedisTemplate<String, OpenAPI> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "openapi:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    /**
     * Fetches an OpenAPI spec from a service URL, using a Redis cache.
     *
     * @param serviceUrl the URL of the service to fetch the spec from
     * @return a Mono emitting the OpenAPI specification, or empty if not found
     */
    public Mono<OpenAPI> fetchOpenApiSpec(String serviceUrl) {
        String cacheKey = CACHE_KEY_PREFIX + serviceUrl;

        return redisTemplate.opsForValue().get(cacheKey)
                .switchIfEmpty(
                        fetchFromService(serviceUrl)
                                .flatMap(spec -> redisTemplate.opsForValue()
                                        .set(cacheKey, spec, CACHE_TTL)
                                        .thenReturn(spec)
                                )
                );
    }

    private Mono<OpenAPI> fetchFromService(String serviceUrl) {
        return webClient.get()
                .uri(serviceUrl + "/v3/api-docs")
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::parseOpenApiSpec)
                .onErrorResume(e -> {
                    log.error("Failed to fetch OpenAPI spec from {}", serviceUrl, e);
                    return Mono.empty();
                });
    }

    private Mono<OpenAPI> parseOpenApiSpec(String json) {
        return Mono.fromCallable(() ->
                objectMapper.readValue(json, OpenAPI.class)
        );
    }
}