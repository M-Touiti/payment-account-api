package com.demo.accountapi.application.service;

import com.demo.accountapi.application.dto.request.LoginRequest;
import com.demo.accountapi.application.dto.request.RefreshTokenRequest;
import com.demo.accountapi.application.dto.request.RegisterRequest;
import com.demo.accountapi.application.dto.response.AuthResponse;
import com.demo.accountapi.application.dto.response.UserResponse;
import com.demo.accountapi.application.port.out.JwtPort;
import com.demo.accountapi.application.port.out.UserRepositoryPort;
import com.demo.accountapi.domain.exception.UserAlreadyExistsException;
import com.demo.accountapi.domain.model.Role;
import com.demo.accountapi.domain.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepositoryPort userRepository;
    private final JwtPort jwtPort;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepositoryPort userRepository, JwtPort jwtPort,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtPort = jwtPort;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already in use: " + request.email());
        }
        User user = User.createNew(
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.USER
        );
        return UserResponse.from(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        String accessToken = jwtPort.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtPort.generateRefreshToken(user.getEmail());
        return AuthResponse.of(accessToken, refreshToken, jwtPort.getAccessTokenExpirationMs());
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String email = jwtPort.extractEmail(request.refreshToken());
        if (!jwtPort.isTokenValid(request.refreshToken(), email)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        String accessToken = jwtPort.generateAccessToken(user.getEmail(), user.getRole().name());
        return AuthResponse.of(accessToken, request.refreshToken(), jwtPort.getAccessTokenExpirationMs());
    }
}
