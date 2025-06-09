package com.deeptechhub.apigateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Configuration class for WebFlux resource handling.
 * Configures resource handlers for Swagger UI and WebJars.
 */
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    /**
     * Registers resource handlers for Swagger UI and WebJars.
     * @param registry the ResourceHandlerRegistry to configure
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                .resourceChain(false);

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .resourceChain(false);
    }
}
