package com.deeptechhub.apigateway.config;

import com.deeptechhub.common.security.JwtServiceImpl;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.util.ResourceUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Spring configuration class for JWT (JSON Web Token) related beans.
 * Provides beans for JWT service and reactive JWT decoder, using properties
 * from the injected {@code JwtProperties}.
 */
@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtProperties jwtProperties;

    @Bean
    public JwtServiceImpl jwtService() throws IOException {
        String secret = Files.readString(ResourceUtils.getFile(jwtProperties.getSecretFile()).toPath()).trim();
        return new JwtServiceImpl(secret, jwtProperties.getAccessTokenExpiryMs(), jwtProperties.getRefreshTokenExpiryMs());
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() throws IOException {
        String secret = Files.readString(Path.of(ResourceUtils.getFile(jwtProperties.getSecretFile()).toURI()), StandardCharsets.UTF_8).trim();
        byte[] decodedKey = Decoders.BASE64.decode(secret);
        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }
}