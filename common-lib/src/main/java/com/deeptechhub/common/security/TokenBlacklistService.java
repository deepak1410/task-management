package com.deeptechhub.common.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistService(@Qualifier("redisTemplate")
                                 RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, Duration ttl) {
        redisTemplate.opsForValue().set(token, "blacklisted", ttl);
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
}
