package com.deeptechhub.identityservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class SecretConfig {

    @Bean
    @Profile("!test") // Only activate this for non-test profiles
    public String jwtSecret(@Value("${jwt.secret-file}") String secretFile) throws IOException {
        return readSecretFile(secretFile, "JWT secret");
    }

    @Bean
    @Profile("!test")
    public String gmailPassword(@Value("${mail.password-file}") String passwordFile) throws IOException {
        return readSecretFile(passwordFile, "Gmail password");
    }

    private String readSecretFile(String filePath, String secretName) throws IOException {
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            throw new RuntimeException(secretName + " file not found at: " + filePath);
        }
        return Files.readString(Paths.get(resource.getURI())).trim();
    }
}

