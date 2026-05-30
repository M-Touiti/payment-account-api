package com.demo.accountapi.unit;

import com.demo.accountapi.application.dto.request.LoginRequest;
import com.demo.accountapi.application.dto.request.RefreshTokenRequest;
import com.demo.accountapi.application.dto.request.RegisterRequest;
import com.demo.accountapi.application.dto.response.AuthResponse;
import com.demo.accountapi.application.dto.response.UserResponse;
import com.demo.accountapi.application.port.out.JwtPort;
import com.demo.accountapi.application.port.out.UserRepositoryPort;
import com.demo.accountapi.application.service.AuthService;
import com.demo.accountapi.domain.exception.UserAlreadyExistsException;
import com.demo.accountapi.domain.model.Role;
import com.demo.accountapi.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private JwtPort jwtPort;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    private AuthService authService;
    private User testUser;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, jwtPort, passwordEncoder, authenticationManager);
        testUser = new User.Builder()
                .id(UUID.randomUUID()).email("user@test.com")
                .passwordHash("hashed").role(Role.USER)
                .createdAt(LocalDateTime.now()).enabled(true).build();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = authService.register(new RegisterRequest("new@test.com", "password123"));

        assertThat(response.email()).isEqualTo("new@test.com");
        assertThat(response.role()).isEqualTo("USER");
        verify(userRepository).save(any());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("existing@test.com", "pass")))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("existing@test.com");
    }

    @Test
    void shouldLoginAndReturnTokens() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(jwtPort.generateAccessToken("user@test.com", "USER")).thenReturn("access-token");
        when(jwtPort.generateRefreshToken("user@test.com")).thenReturn("refresh-token");
        when(jwtPort.getAccessTokenExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.login(new LoginRequest("user@test.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void shouldRefreshWithValidToken() {
        when(jwtPort.extractEmail("valid-refresh")).thenReturn("user@test.com");
        when(jwtPort.isTokenValid("valid-refresh", "user@test.com")).thenReturn(true);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(jwtPort.generateAccessToken("user@test.com", "USER")).thenReturn("new-access-token");
        when(jwtPort.getAccessTokenExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.refresh(new RefreshTokenRequest("valid-refresh"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("valid-refresh");
    }

    @Test
    void shouldThrowOnInvalidRefreshToken() {
        when(jwtPort.extractEmail("bad-token")).thenReturn("user@test.com");
        when(jwtPort.isTokenValid("bad-token", "user@test.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("bad-token")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired");
    }
}
