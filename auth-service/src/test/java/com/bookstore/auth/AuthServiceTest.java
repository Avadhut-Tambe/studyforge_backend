package com.bookstore.auth;

import com.bookstore.auth.dto.LoginRequest;
import com.bookstore.auth.dto.RegisterRequest;
import com.bookstore.auth.exception.AuthException;
import com.bookstore.auth.model.User;
import com.bookstore.auth.repository.UserRepository;
import com.bookstore.auth.service.AuthService;
import com.bookstore.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository  userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil          jwtUtil;

    @InjectMocks private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
            .id("user-123")
            .email("test@example.com")
            .name("Test User")
            .passwordHash("$2a$12$hashedpassword")
            .role(User.Role.BUYER)
            .status(User.Status.ACTIVE)
            .createdAt(Instant.now())
            .build();
    }

    // ── Registration ────────────────────────────────────────

    @Test
    void register_success_buyer() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail("new@example.com");
        req.setPassword("password123");
        req.setRole("BUYER");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$hashed");
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("new-id");
            return u;
        });
        when(jwtUtil.generateAccessToken(any())).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh_token");
        when(jwtUtil.getAccessTokenExpiry()).thenReturn(3600L);

        var response = authService.register(req);

        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getUser().getRole()).isEqualTo("BUYER");
        assertThat(response.getUser().getStatus()).isEqualTo("ACTIVE");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_seller_gets_pending_approval() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Seller User");
        req.setEmail("seller@example.com");
        req.setPassword("password123");
        req.setRole("SELLER");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("seller-id");
            return u;
        });
        when(jwtUtil.generateAccessToken(any())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh");
        when(jwtUtil.getAccessTokenExpiry()).thenReturn(3600L);

        var response = authService.register(req);

        assertThat(response.getUser().getStatus()).isEqualTo("PENDING_APPROVAL");
    }

    @Test
    void register_fails_when_email_already_exists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existing@example.com");
        req.setRole("BUYER");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
            .isInstanceOf(AuthException.class)
            .hasMessageContaining("already registered");
    }

    @Test
    void register_fails_for_admin_role() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("admin@example.com");
        req.setRole("ADMIN");

        when(userRepository.existsByEmail(any())).thenReturn(false);

        assertThatThrownBy(() -> authService.register(req))
            .isInstanceOf(AuthException.class)
            .hasMessageContaining("Cannot self-register as ADMIN");
    }

    // ── Login ────────────────────────────────────────────────

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("password123", sampleUser.getPasswordHash())).thenReturn(true);
        when(jwtUtil.generateAccessToken(sampleUser)).thenReturn("access");
        when(jwtUtil.generateRefreshToken(sampleUser)).thenReturn("refresh");
        when(jwtUtil.getAccessTokenExpiry()).thenReturn(3600L);

        var response = authService.login(req);

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void login_fails_with_wrong_password() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("wrongpass");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongpass", sampleUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(AuthException.class)
            .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_fails_for_unknown_email() {
        LoginRequest req = new LoginRequest();
        req.setEmail("ghost@example.com");
        req.setPassword("pass");

        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(AuthException.class);
    }

    @Test
    void login_fails_for_suspended_user() {
        sampleUser.setStatus(User.Status.SUSPENDED);
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(AuthException.class)
            .hasMessageContaining("suspended");
    }

    @Test
    void login_fails_for_pending_seller() {
        sampleUser.setRole(User.Role.SELLER);
        sampleUser.setStatus(User.Status.PENDING_APPROVAL);
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(AuthException.class)
            .hasMessageContaining("pending");
    }
}
