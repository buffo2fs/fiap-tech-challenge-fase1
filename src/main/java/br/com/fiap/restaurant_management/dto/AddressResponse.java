package br.com.fiap.restaurant_management.dto;

public record AddressResponse(
        String street,
        String number,
        String city,
        String zipCode
) {
}
