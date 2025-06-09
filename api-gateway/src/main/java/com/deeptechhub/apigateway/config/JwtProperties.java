package com.deeptechhub.apigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secretFile;
    private long accessTokenExpiryMs;
    private long refreshTokenExpiryMs = 604800000; // default: 7 days
    private List<String> excludePaths;
}
