package br.com.fiap.restaurant_management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Restaurant Management API")
                        .version("v1")
                        .description("""
                                User management API for a shared restaurant management system.
                                Support customers and restaurants owners: registration, search by name, profile update,
                                password change, soft delete and login validation.
                                Errors follow RFC 7807 (ProblemDetail).
                                """));
    }
}
