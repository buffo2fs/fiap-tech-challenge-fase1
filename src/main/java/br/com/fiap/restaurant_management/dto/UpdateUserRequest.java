package br.com.fiap.restaurant_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Schema(description = "User's full name", example = "John Doe")
        @NotBlank @Size(max = 150) String name,

        @Schema(description = "User's email address", example = "john.doe@example.com")
        @NotBlank @Email @Size(max = 255) String email,

        @Schema(description = "User's login", example = "Jhon.Doe")
        @NotBlank @Size(min = 3, max = 50) String login,

        @Schema(description = "User's address", example = "123 Main St, City, State 12345")
        @NotNull @Valid AddressRequest address
) {
}
