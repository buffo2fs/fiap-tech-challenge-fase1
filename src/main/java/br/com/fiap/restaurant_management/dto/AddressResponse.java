package br.com.fiap.restaurant_management.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AddressResponse(
        @Schema(description = "Street name", example = "Rua dos Gatos")
        String street,

        @Schema(description = "Street number", example = "123")
        String number,

        @Schema(description = "City name", example = "São Paulo")
        String city,

        @Schema(description = "ZIP code", example = "01310-100")
        String zipCode
) {
}