package br.com.fiap.restaurant_management.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;


@Embeddable
public class Address {

    @Column(name = "street", nullable = false, length = 150)
    private String street;

    @Column(name = "address_number", nullable = false, length = 20)
    private String number;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "zip_code", nullable = false, length = 9)
    private String zipCode;

    protected Address() {
    }

    public Address(String street, String number, String city, String zipCode) {
        this.street = street;
        this.number = number;
        this.city = city;
        this.zipCode = zipCode;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getCity() {
        return city;
    }

    public String getZipCode() {
        return zipCode;
    }
}
