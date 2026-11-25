package br.com.fiap.restaurant_management.service;

import br.com.fiap.restaurant_management.dto.LoginRequest;
import br.com.fiap.restaurant_management.dto.LoginResponse;
import br.com.fiap.restaurant_management.exception.InvalidCredentialsException;
import br.com.fiap.restaurant_management.mapper.UserMapper;
import br.com.fiap.restaurant_management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid login or password.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.tokenService = tokenService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        var user = userRepository.findByLogin(request.login())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS));

        String accessToken = tokenService.generate(user);
        long expiresIn = tokenService.expiresInSeconds();

        return new LoginResponse("Login successful.", tokenService.generate(user), "Bearer", tokenService.expiresInSeconds(), userMapper.toResponse(user));
    }
}
