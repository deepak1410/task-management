package com.deeptechhub.common.utils;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Utility class for generating OpenAPI documentation configuration.
 */
public class SwaggerUtils {
    /**
     * Creates an OpenAPI configuration for a given service.
     * @param serviceName name of the service
     * @param description brief API description
     * @return configured OpenAPI instance
     */
    public static OpenAPI createOpenAPI(String serviceName, String description) {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Task Management API - " + serviceName)
                        .description(description)
                        .version("v1.0")
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Task management Wiki")
                        .url("https://github.com/deepak1410/task-management/blob/main/README.md"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

}
