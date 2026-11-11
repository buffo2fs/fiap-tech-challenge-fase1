package br.com.fiap.restaurant_management.service;

import br.com.fiap.restaurant_management.domain.Address;
import br.com.fiap.restaurant_management.domain.User;
import br.com.fiap.restaurant_management.domain.UserType;
import br.com.fiap.restaurant_management.dto.LoginRequest;
import br.com.fiap.restaurant_management.dto.LoginResponse;
import br.com.fiap.restaurant_management.exception.InvalidCredentialsException;
import br.com.fiap.restaurant_management.mapper.UserMapper;
import br.com.fiap.restaurant_management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String LOGIN = "maria.souza";
    private static final String PASSWORD = "Forte@789";
    private static final String HASH = "hashed-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private User existingUser() {
        Address address = new Address("Avenida Paulista", "1000", "São Paulo", "01310-100");
        return new User("Maria Souza", "maria@example.com", LOGIN, HASH, UserType.CUSTOMER, address);
    }

    @Test
    void shouldLoginWithValidCredentials() {
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);

        LoginResponse response = authService.login(new LoginRequest(LOGIN, PASSWORD));

        assertThat(response.user().login()).isEqualTo(LOGIN);
    }

    @Test
    void shouldRejectWrongPassword() {
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches("Errada@000", HASH)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(LOGIN, "Errada@000")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid login or password.");
    }

    @Test
    void shouldRejectUnknownLoginWithSameMessage() {
        when(userRepository.findByLogin("nao.existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nao.existe", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid login or password.");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}