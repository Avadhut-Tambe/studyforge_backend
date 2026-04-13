package com.bookstore.auth.util;

import com.bookstore.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * JWT utility for generating and validating access/refresh tokens.
 * Uses HS256 with a shared secret. Production systems should use RS256 with
 * a key pair (private key in auth-service, public key distributed to others).
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiry-seconds:3600}")        // 1 hour
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry-seconds:604800}")     // 7 days
    private long refreshTokenExpiry;

    public String generateAccessToken(User user) {
        return buildToken(user, accessTokenExpiry);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshTokenExpiry);
    }

    private String buildToken(User user, long expirySeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(user.getId())
            .claim("email", user.getEmail())
            .claim("name",  user.getName())
            .claim("role",  user.getRole().name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(expirySeconds)))
            .signWith(getSigningKey())
            .compact();
    }

    public Claims validateAndParseClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            validateAndParseClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String extractUserId(String token) {
        return validateAndParseClaims(token).getSubject();
    }

    public long getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
