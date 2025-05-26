package com.deeptechhub.taskservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    public String jwtSecret() {
        return "dGVzdC1zZWNyZXQtMTIzNDU2Nzg5MC1mb3ItdGVzdGluZy1vbmx5";
    }

    @Bean
    public String gmailPassword() {
        return "test-gmail-password";
    }
}
