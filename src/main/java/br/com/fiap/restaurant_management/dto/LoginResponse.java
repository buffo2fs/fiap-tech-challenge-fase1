package br.com.fiap.restaurant_management.dto;

public record LoginResponse(
        String message,
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
