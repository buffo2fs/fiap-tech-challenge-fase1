package br.com.fiap.restaurant_management.service;

import br.com.fiap.restaurant_management.domain.Address;
import br.com.fiap.restaurant_management.domain.User;
import br.com.fiap.restaurant_management.domain.UserType;
import br.com.fiap.restaurant_management.dto.*;
import br.com.fiap.restaurant_management.exception.DuplicateResourceException;
import br.com.fiap.restaurant_management.exception.InvalidCredentialsException;
import br.com.fiap.restaurant_management.exception.ResourceNotFoundException;
import br.com.fiap.restaurant_management.mapper.UserMapper;
import br.com.fiap.restaurant_management.repository.UserRepository;
import org.hibernate.sql.Update;
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

    private UpdateUserRequest updateRequest(String email, String login) {
        return new UpdateUserRequest(
                "Francisca Sousa",
                email,
                login,
                new AddressRequest("Rua Nova", "500", "Campinas", "87657-888"));
    }

    @Test
    void shouldUpdateUserProfileWithoutChangingPassword() {
        User user = existingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("novo@example.com", 1L)).thenReturn(false);
        when(userRepository.existsByLoginAndIdNot("novo.login", 1L)).thenReturn(false);
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        UserResponse response = userService.update(1L, updateRequest(" Novo@Example.com ", "novo.login"));
        assertThat(response.name()).isEqualTo("Francisca Sousa");
        assertThat(response.login()).isEqualTo("novo.login");
        assertThat(response.email()).isEqualTo("novo@example.com");
        assertThat(response.address().city()).isEqualTo("Campinas");
        assertThat(user.getPasswordHash()).isEqualTo("hashed-password");
    }

    @Test
    void shouldRejectUpdateWithEmailFromAnotherUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));
        when(userRepository.existsByEmailAndIdNot("outro@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, updateRequest("outro@example.com", "novo.login")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectUpdateWithLoginFromAnotherUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));
        when(userRepository.existsByEmailAndIdNot("novo@example.com", 1L)).thenReturn(false);
        when(userRepository.existsByLoginAndIdNot("outro.login", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, updateRequest("novo@example.com", "outro.login")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowWhenUpdatingUnknowUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(999L, updateRequest("novo@example.com", "novo.login")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shoudlChangePasswordWhenCurrentPasswordMatches() {
        User user = existingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, "hashed-password")).thenReturn(true);
        when(passwordEncoder.encode("NovaSenha@123")).thenReturn("new-hashed-password");

        userService.changePassword(1L, new ChangePasswordRequest(PASSWORD, "NovaSenha@123"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hashed-password");
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void shouldRejectPasswordChangeWhenCurrentPasswordIsWrong() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches("errado345", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, new ChangePasswordRequest("errado345", "NovaSenha@123")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldThrowWhenPasswordOfUnknownUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword(999L, new ChangePasswordRequest(PASSWORD, "NovaSenha@123")))
                .isInstanceOf(ResourceNotFoundException.class);

    }
}
