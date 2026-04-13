package com.bookstore.auth.service;

import com.bookstore.auth.dto.AuthResponse;
import com.bookstore.auth.dto.LoginRequest;
import com.bookstore.auth.dto.RegisterRequest;
import com.bookstore.auth.exception.AuthException;
import com.bookstore.auth.model.User;
import com.bookstore.auth.repository.UserRepository;
import com.bookstore.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Register a new user (BUYER or SELLER).
     * Sellers get PENDING_APPROVAL status until an admin approves them.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered");
        }

        User.Role role;
        try {
            role = User.Role.valueOf(request.getRole().toUpperCase());
            if (role == User.Role.ADMIN) {
                throw new AuthException("Cannot self-register as ADMIN");
            }
        } catch (IllegalArgumentException e) {
            throw new AuthException("Invalid role. Allowed: BUYER, SELLER");
        }

        User.Status status = role == User.Role.SELLER
            ? User.Status.PENDING_APPROVAL
            : User.Status.ACTIVE;

        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .phone(request.getPhone())
            .role(role)
            .status(status)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        User savedUser = userRepository.save(user);
        log.info("New {} registered: {} [{}]", role, savedUser.getEmail(), savedUser.getId());

        return buildAuthResponse(savedUser);
    }

    /**
     * Authenticate user and return JWT tokens.
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }

        if (user.getStatus() == User.Status.SUSPENDED) {
            throw new AuthException("Account suspended. Contact support.");
        }

        if (user.getStatus() == User.Status.PENDING_APPROVAL) {
            throw new AuthException("Your seller account is pending admin approval.");
        }

        log.info("User logged in: {} [{}]", user.getEmail(), user.getRole());
        return buildAuthResponse(user);
    }

    /**
     * Refresh tokens using a valid refresh token.
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new AuthException("Invalid or expired refresh token");
        }

        String userId = jwtUtil.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AuthException("User not found"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtUtil.getAccessTokenExpiry())
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .avatarUrl(user.getAvatarUrl())
                .build())
            .build();
    }
}
