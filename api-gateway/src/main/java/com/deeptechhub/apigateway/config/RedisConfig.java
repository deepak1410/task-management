package com.deeptechhub.apigateway.config;

import com.deeptechhub.apigateway.util.JsonRedisSerializer;
import com.deeptechhub.common.security.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configures Redis beans for string operations, token blacklist, and OpenAPI storage.
 */
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public TokenBlacklistService tokenBlacklistService(StringRedisTemplate redisTemplate) {
        return new TokenBlacklistService(redisTemplate);
    }

    /**
     * Provides a ReactiveRedisTemplate for OpenAPI storage.
     */
    @Bean
    public ReactiveRedisTemplate<String, OpenAPI> openApiRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            ObjectMapper objectMapper) {

        RedisSerializationContext<String, OpenAPI> context =
                RedisSerializationContext.<String, OpenAPI>newSerializationContext()
                        .key(new StringRedisSerializer())
                        .value(new JsonRedisSerializer<>(objectMapper, OpenAPI.class))
                        .hashKey(new StringRedisSerializer())
                        .hashValue(new GenericToStringSerializer<>(OpenAPI.class))
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

}
