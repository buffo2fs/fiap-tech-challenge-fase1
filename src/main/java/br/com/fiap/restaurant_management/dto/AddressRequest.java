package br.com.fiap.restaurant_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Schema(description = "Street name", example = "Rua dos Gatos")
        @NotBlank @Size(max = 150) String street,

        @Schema(description = "Street number", example = "123")
        @NotBlank @Size(max = 20) String number,

        @Schema(description = "City name", example = "São Paulo")
        @NotBlank @Size(max = 100) String city,

        @Schema(description = "ZIP code", example = "01310-100")
        @NotBlank @Pattern(regexp = "\\d{5}-?\\d{3}", message = "must be a valid CEP (00000-000)")
        String zipCode
) {
}
