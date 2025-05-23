package com.deeptechhub.identityservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private long accessTokenExpiryMs; // Used for authentication
    private long refreshTokenExpiryMs; // Used for generating new access token
}
