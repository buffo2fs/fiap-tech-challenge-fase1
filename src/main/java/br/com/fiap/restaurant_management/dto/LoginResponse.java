package br.com.fiap.restaurant_management.dto;

public record LoginResponse(
        String message,
        UserResponse user
) {
}
