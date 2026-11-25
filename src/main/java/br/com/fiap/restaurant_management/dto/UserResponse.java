package br.com.fiap.restaurant_management.dto;

import br.com.fiap.restaurant_management.domain.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record UserResponse(
        @Schema(description = "User id", example = "1")
        Long id,

        @Schema(description = "User's full name", example = "John Doe")
        String name,

        @Schema(description = "User's email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "User's login", example = "john.doe")
        String login,

        @Schema(description = "User's type", example = "CUSTOMER")
        UserType type,

        @Schema(description = "User's address")
        AddressResponse address,

        @Schema(description = "Last modification date", example = "2026-11-25T19:30:00")
        LocalDateTime lastModifiedAt
) {
}