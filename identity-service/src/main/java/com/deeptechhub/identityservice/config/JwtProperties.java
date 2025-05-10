package com.deeptechhub.identityservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String secret;
    private long accessTokenExpiryMs; // Used for authentication
    private long refreshTokenExpiryMs; // Used for generating new access token

}
