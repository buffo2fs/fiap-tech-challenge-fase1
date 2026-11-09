package br.com.fiap.restaurant_management.dto;

import br.com.fiap.restaurant_management.domain.UserType;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        String login,
        UserType type,
        AddressResponse address,
        LocalDateTime lastModifiedAt
) {
}
