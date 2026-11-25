package br.com.fiap.restaurant_management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Restaurant Management API")
                        .version("v1")
                        .description("""
                                User management API for a shared restaurant management system.
                                Supports customers and restaurant owners: registration, search by name,
                                profile update, password change, soft delete and login validation.
                                Protected endpoints require a JWT: call POST /api/v1/auth/login,
                                then click Authorize and paste the accessToken.
                                Errors follow RFC 7807 (ProblemDetail)."""))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    private static final String BEARER_AUTH = "bearerAuth";
}
