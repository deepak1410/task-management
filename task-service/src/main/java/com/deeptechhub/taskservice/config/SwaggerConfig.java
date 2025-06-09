package com.deeptechhub.taskservice.config;

import com.deeptechhub.common.utils.SwaggerUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI taskManagementAPI() {
        return SwaggerUtils.createOpenAPI("task-service", "API for user's tasks management");
    }
}
