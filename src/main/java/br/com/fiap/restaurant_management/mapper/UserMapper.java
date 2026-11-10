package br.com.fiap.restaurant_management.mapper;

import br.com.fiap.restaurant_management.domain.Address;
import br.com.fiap.restaurant_management.domain.User;
import br.com.fiap.restaurant_management.dto.AddressRequest;
import br.com.fiap.restaurant_management.dto.AddressResponse;
import br.com.fiap.restaurant_management.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public Address toAddress(AddressRequest request) {
        return new Address(request.street(), request.number(), request.city(), request.zipCode()
        );
    }

    public UserResponse toResponse (User user) {
        Address address = user.getAddress();
        AddressResponse addressResponse = new AddressResponse(
                address.getStreet(), address.getNumber(), address.getCity(), address.getZipCode()
        );

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getType(),
                addressResponse,
                user.getLastModifiedAt()
        );
    }
}
