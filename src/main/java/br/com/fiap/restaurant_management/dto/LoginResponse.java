package br.com.fiap.restaurant_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "Result message", example = "Login successful.")
        String message,

        @Schema(description = "JWT to send in the Authorization header", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Token type", example = "Bearer")
        String tokenType,

        @Schema(description = "Token lifetime in seconds", example = "3600")
        long expiresIn,

        @Schema(description = "Authenticated user")
        UserResponse user
) {
}