package br.com.fiap.restaurant_management.controller;

import br.com.fiap.restaurant_management.domain.UserType;
import br.com.fiap.restaurant_management.dto.AddressResponse;
import br.com.fiap.restaurant_management.dto.CreateUserRequest;
import br.com.fiap.restaurant_management.dto.UserResponse;
import br.com.fiap.restaurant_management.exception.DuplicateResourceException;
import br.com.fiap.restaurant_management.exception.ResourceNotFoundException;
import br.com.fiap.restaurant_management.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final String VALID_BODY = """
            {
              "name": "Carla Mendes",
              "email": "carla@example.com",
              "login": "carla.mendes",
              "password": "Senha@123",
              "type": "CUSTOMER",
              "address": {
                "street": "Rua das Flores",
                "number": "42",
                "city": "Campinas",
                "zipCode": "13010-000"
              }
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UserResponse response() {
        AddressResponse address = new AddressResponse("Rua das Flores", "42", "Campinas", "13010-000");
        return new UserResponse(1L, "Carla Mendes", "carla@example.com", "carla.mendes",
                UserType.CUSTOMER, address, LocalDateTime.now());
    }

    @Test
    void shouldCreateUserAndReturn201() throws Exception {
        when(userService.create(any(CreateUserRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/users/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturn400WhenFieldsAreInvalid() throws Exception {
        String invalidBody = """
                {
                  "name": "",
                  "email": "email-invalido",
                  "login": "ab",
                  "password": "123",
                  "type": "CUSTOMER",
                  "address": {
                    "street": "Rua das Flores",
                    "number": "42",
                    "city": "Campinas",
                    "zipCode": "123"
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        when(userService.create(any(CreateUserRequest.class)))
                .thenThrow(new DuplicateResourceException("User with email carla@example.com already exists"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturnUserById() throws Exception {
        when(userService.findById(1L)).thenReturn(response());

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("carla.mendes"));
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        when(userService.findById(999L))
                .thenThrow(new ResourceNotFoundException("User with id 999 not found"));

        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User with id 999 not found"));
    }

    @Test
    void shouldDeleteUserAndReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    @Test
    void shouldReturn404WhenDeletingUnknownUser() throws Exception {
        doThrow(new ResourceNotFoundException("User with id 999 not found"))
                .when(userService).delete(999L);

        mockMvc.perform(delete("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }
}