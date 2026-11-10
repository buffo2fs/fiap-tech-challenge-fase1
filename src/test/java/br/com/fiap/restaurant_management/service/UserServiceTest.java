package br.com.fiap.restaurant_management.service;

import br.com.fiap.restaurant_management.domain.User;
import br.com.fiap.restaurant_management.domain.UserType;
import br.com.fiap.restaurant_management.dto.AddressRequest;
import br.com.fiap.restaurant_management.dto.CreateUserRequest;
import br.com.fiap.restaurant_management.dto.UserResponse;
import br.com.fiap.restaurant_management.exception.DuplicateResourceException;
import br.com.fiap.restaurant_management.mapper.UserMapper;
import br.com.fiap.restaurant_management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "joao@example.com";
    private static final String LOGIN = "joao.pereira";
    private static final String PASSWORD = "Segura@456";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private UserMapper userMapper = new UserMapper();

    @InjectMocks
    private UserService userService;

    private CreateUserRequest request(String email) {
        return new CreateUserRequest(
                "João Pereira",
                email,
                LOGIN,
                PASSWORD,
                UserType.RESTAURANT_OWNER,
                new AddressRequest("Rua Augusta", "250", "São Paulo", "01305-000"));
    }

    @Test
    void shouldCreateUserWithHashedPasswordAndNormalizedEmail() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByLogin(LOGIN)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.create(request(" Joao@Example.com "));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.type()).isEqualTo(UserType.RESTAURANT_OWNER);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request(EMAIL)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("User with email " + EMAIL + " already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateLogin() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByLogin(LOGIN)).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request(EMAIL)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Login is already in use.");

        verify(userRepository, never()).save(any());
    }
}