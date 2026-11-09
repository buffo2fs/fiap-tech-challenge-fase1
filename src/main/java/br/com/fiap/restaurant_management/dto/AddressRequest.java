package br.com.fiap.restaurant_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 150) String street,
        @NotBlank @Size(max = 20) String number,
        @NotBlank @Size(max = 100) String city,
        @NotBlank
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "must be a valid CEP (0000-000)")
        String zipCode
) {
}
