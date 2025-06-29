package com.deeptechhub.apigateway.ratelimiter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "rate-limiting")
@Data
public class RateLimitConfigProperties {
    private List<ServiceRateLimit> services;

    @Data
    public static class ServiceRateLimit {
        private String routeId;
        private int replenishRate;
        private int burstCapacity;
    }

    public int getReplenishRateForRoute(String routeId) {
        return services.stream()
                .filter(s -> s.getRouteId().equals(routeId))
                .map(ServiceRateLimit::getReplenishRate)
                .findFirst()
                .orElse(5); // default
    }

    public int getBurstCapacityForRoute(String routeId) {
        return services.stream()
                .filter(s -> s.getRouteId().equals(routeId))
                .map(ServiceRateLimit::getBurstCapacity)
                .findFirst()
                .orElse(10); // default
    }

}
