package br.com.fiap.restaurant_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "User's login", example = "Jhon.Doe")
        @NotBlank String login,
        @Schema(description = "User's password", example = "Secure@123")
        @NotBlank String password
) {
}
