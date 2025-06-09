package com.deeptechhub.apigateway.config;

import com.deeptechhub.apigateway.util.SecuritySchemeDeserializer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for customizing ObjectMapper used in OpenAPI documentation.
 * Registers modules for JavaTime and custom SecurityScheme deserialization,
 * and configures serialization/deserialization features.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures an ObjectMapper for OpenAPI documentation.
     * Supports JavaTime, custom SecurityScheme deserialization, and relaxed parsing.
     * @return a configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(SecurityScheme.class, new SecuritySchemeDeserializer());

        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(module)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
    }
}
