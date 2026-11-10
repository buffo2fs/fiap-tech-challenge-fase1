package br.com.fiap.restaurant_management.service;

import br.com.fiap.restaurant_management.domain.Address;
import br.com.fiap.restaurant_management.domain.User;
import br.com.fiap.restaurant_management.domain.UserType;
import br.com.fiap.restaurant_management.dto.AddressRequest;
import br.com.fiap.restaurant_management.dto.CreateUserRequest;
import br.com.fiap.restaurant_management.dto.PageResponse;
import br.com.fiap.restaurant_management.dto.UserResponse;
import br.com.fiap.restaurant_management.exception.DuplicateResourceException;
import br.com.fiap.restaurant_management.exception.ResourceNotFoundException;
import br.com.fiap.restaurant_management.mapper.UserMapper;
import br.com.fiap.restaurant_management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

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

    private User existingUser() {
        return new User(
                "Francisca Sousa",
                EMAIL,
                LOGIN,
                "hashed-password",
                UserType.RESTAURANT_OWNER,
                new Address("Av Paulista", "1000", "São Paulo", "01111-111"));
    }

    @Test
    void shouldFindUserById() {
        User user = existingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));

        UserResponse response = userService.findById(1L);

        assertThat(response.login()).isEqualTo(LOGIN);
        assertThat(response.email()).isEqualTo(EMAIL);
    }

    @Test
    void shouldThrowWhenUserIsNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldSearchUserByTrimmedName() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByNameContainingIgnoreCase("francisca", pageable))
                .thenReturn(new PageImpl<>(List.of(existingUser()), pageable, 1));

    PageResponse<UserResponse> result = userService.searchByName(" francisca ", pageable);

    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).login()).isEqualTo(LOGIN);
    assertThat(result.totalElements()).isEqualTo(1);
    }
}