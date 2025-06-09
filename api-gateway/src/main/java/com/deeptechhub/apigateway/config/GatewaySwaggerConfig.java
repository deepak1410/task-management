package com.deeptechhub.apigateway.config;

import com.deeptechhub.apigateway.service.OpenApiService;
import com.deeptechhub.common.utils.SwaggerUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
* Configuration class for setting up Swagger (OpenAPI) documentation for the API Gateway.
* Provides OpenAPI documentation for the gateway itself and integrates OpenAPI specs
* from microservices such as Identity and Task services.
*/
@Configuration
public class GatewaySwaggerConfig {
    private final OpenApiService openApiService;
    private final String identityServiceUrl;
    private final String taskServiceUrl;

    public GatewaySwaggerConfig(OpenApiService openApiService,
                                @Value("${identity-service.url}") String identityServiceUrl,
                                @Value("${task-service.url}") String taskServiceUrl) {
        this.openApiService = openApiService;
        this.identityServiceUrl = identityServiceUrl;
        this.taskServiceUrl = taskServiceUrl;
    }

    /**
     * Creates the main OpenAPI documentation for the API Gateway.
     * @return the OpenAPI object describing the API Gateway
     */
    @Bean
    public OpenAPI gatewayOpenAPI() {
        return SwaggerUtils.createOpenAPI("API-Gateway", "Central entrypoint for all microservices");
    }

    @Bean
    public GroupedOpenApi allApisGroup() {
        return GroupedOpenApi.builder()
                .group("all-services")
                .pathsToMatch("/**")
                .build();
    }

    /**
     * Groups and customizes OpenAPI documentation for the Identity Service.
     * Integrates OpenAPI spec from the Identity Service endpoint.
     * @return a GroupedOpenApi bean for the Identity Service
     */
    @Bean
    public GroupedOpenApi identityApiGroup() {
        return GroupedOpenApi.builder()
            .group("identity-service")
            .pathsToMatch("api/auth/**, /api/users/**", "/auth/**", "/users/**")
            .addOpenApiCustomizer(openApi -> {
                openApiService.fetchOpenApiSpec(identityServiceUrl)
                    .subscribe(identitySpec -> {
                        if (identitySpec != null) {
                            openApi.paths(identitySpec.getPaths());
                            openApi.components(identitySpec.getComponents());
                        }
                    });
            })
            .build();
    }

    /**
     * Groups and customizes OpenAPI documentation for the Task Service.
     * Integrates OpenAPI spec from the Task Service endpoint.
     * @return a GroupedOpenApi bean for the Task Service
     */
    @Bean
    public GroupedOpenApi taskApiGroup() {
        return GroupedOpenApi.builder()
                .group("task-service")
                .pathsToMatch("/api/tasks/**", "/tasks/**")
                .addOpenApiCustomizer(openApi -> {
                    openApiService.fetchOpenApiSpec(taskServiceUrl)
                        .subscribe(taskSpec -> {
                            if (taskSpec != null) {
                                openApi.paths(taskSpec.getPaths());
                                openApi.components(taskSpec.getComponents());
                            }
                        });
                })
                .build();
    }

}
