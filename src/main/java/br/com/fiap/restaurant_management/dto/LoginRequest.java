package br.com.fiap.restaurant_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "User's login", example = "john.doe")
        @NotBlank String login,

        @Schema(description = "User's password", example = "SecurePass123")
        @NotBlank String password
) {
}