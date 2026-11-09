package br.com.fiap.restaurant_management.dto;

import br.com.fiap.restaurant_management.domain.UserType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 3, max = 50) String login,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull UserType type,
        @NotNull @Valid AddressRequest address
        ) {
}
