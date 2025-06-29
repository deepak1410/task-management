package com.deeptechhub.apigateway.ratelimiter;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getHeaders()
                    .getFirst("X-Forwarded-For");

            if (ip == null) {
                ip = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                        .map(addr -> addr.getAddress().getHostAddress())
                        .orElse("unknown");
            }
            return Mono.just(ip);
        };
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // default only if not using dynamic per-route
        return new RedisRateLimiter(5, 10);
    }

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveStringRedisTemplate(factory);
    }

}
