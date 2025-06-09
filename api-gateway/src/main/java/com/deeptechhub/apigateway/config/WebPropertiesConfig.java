package com.deeptechhub.apigateway.config;

import com.deeptechhub.apigateway.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes {@link WebProperties} and its {@link Resources} as beans for injection.
 * <p>
 * Required by {@link GlobalExceptionHandler} for resource configuration.
 */
@Configuration
public class WebPropertiesConfig {

    /**
     * Provides web properties configuration.
     * @return new {@link WebProperties} instance
     */
    @Bean
    public WebProperties webProperties() {
        return new WebProperties();
    }

    /**
     * Provides access to resource handling configuration.
     * @param webProperties the source for resources
     * @return the {@link Resources} configuration
     */
    @Bean
    public Resources webPropertiesResources(WebProperties webProperties) {
        return webProperties.getResources();
    }
}

