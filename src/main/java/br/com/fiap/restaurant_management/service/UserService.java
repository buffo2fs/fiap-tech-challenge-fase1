package br.com.fiap.restaurant_management.service;

import br.com.fiap.restaurant_management.domain.User;
import br.com.fiap.restaurant_management.dto.*;
import br.com.fiap.restaurant_management.exception.DuplicateResourceException;
import br.com.fiap.restaurant_management.exception.InvalidCredentialsException;
import br.com.fiap.restaurant_management.exception.ResourceNotFoundException;
import br.com.fiap.restaurant_management.mapper.UserMapper;
import br.com.fiap.restaurant_management.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = User.normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User with email " + email + " already exists");
        }
        if (userRepository.existsByLogin(request.login())) {
            throw new DuplicateResourceException("Login is already in use.");
        }

        User user = new User(
                request.name(),
                email,
                request.login(),
                passwordEncoder.encode(request.password()),
                request.type(),
                userMapper.toAddress(request.address()));

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userMapper.toResponse(getUserOrThrow(id));
    }

    @Transactional
    public PageResponse<UserResponse> searchByName(String name, Pageable pageable) {
        return PageResponse.from(
                userRepository.findByNameContainingIgnoreCase(name.trim(), pageable)
                        .map(userMapper::toResponse));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = getUserOrThrow(id);
        String email = User.normalizeEmail(request.email());

        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateResourceException("User with email " + email + " already exists");
        }
        if (userRepository.existsByLoginAndIdNot(request.login(), id)) {
            throw new DuplicateResourceException("Login is already in use.");
        }

        user.updateProfile(request.name(), email, request.login(),
                userMapper.toAddress(request.address()));

        return userMapper.toResponse(userRepository.saveAndFlush(user));
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = getUserOrThrow(id);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect.");
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.saveAndFlush(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = getUserOrThrow(id);
        user.markAsDeleted();
        userRepository.saveAndFlush(user);
    }


}
