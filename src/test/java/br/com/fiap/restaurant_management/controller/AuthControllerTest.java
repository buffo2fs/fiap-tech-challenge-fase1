package br.com.fiap.restaurant_management.controller;

import br.com.fiap.restaurant_management.config.ProblemDetailAuthenticationEntryPoint;
import br.com.fiap.restaurant_management.config.SecurityConfig;
import br.com.fiap.restaurant_management.domain.UserType;
import br.com.fiap.restaurant_management.dto.AddressResponse;
import br.com.fiap.restaurant_management.dto.LoginRequest;
import br.com.fiap.restaurant_management.dto.LoginResponse;
import br.com.fiap.restaurant_management.dto.UserResponse;
import br.com.fiap.restaurant_management.exception.InvalidCredentialsException;
import br.com.fiap.restaurant_management.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, ProblemDetailAuthenticationEntryPoint.class})
class AuthControllerTest {

    private static final String LOGIN_BODY = """
            {
              "login": "carla.mendes",
              "password": "Senha@123"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldReturn200WhenCredentialsAreValid() throws Exception {
        AddressResponse address = new AddressResponse("Rua das Flores", "42", "Campinas", "13010-000");
        UserResponse user = new UserResponse(1L, "Carla Mendes", "carla@example.com", "carla.mendes",
                UserType.CUSTOMER, address, LocalDateTime.now());
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("Login successful.", "fake-token", "Bearer", 3600L, user));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful."))
                .andExpect(jsonPath("$.accessToken").value("fake-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.login").value("carla.mendes"));
    }

    @Test
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid login or password."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid login or password."));
    }

    @Test
    void shouldReturn400WhenLoginIsBlank() throws Exception {
        String blankBody = """
                {
                  "login": "",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blankBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.login").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verifyNoInteractions(authService);
    }
}