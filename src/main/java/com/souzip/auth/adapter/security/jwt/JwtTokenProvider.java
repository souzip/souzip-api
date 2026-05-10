package com.souzip.auth.adapter.security.jwt;

import com.souzip.auth.application.required.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider implements TokenProvider {

    private final JwtProperties jwtProperties;

    @Override
    public String generateAccessToken(Long userId) {
        return createToken(String.valueOf(userId), jwtProperties.getExpiration());
    }

    @Override
    public String generateRefreshToken() {
        return createToken("refresh", jwtProperties.getRefreshExpiration());
    }

    @Override
    public LocalDateTime getRefreshTokenExpiresAt() {
        return LocalDateTime.now().plus(jwtProperties.getRefreshExpiration(), ChronoUnit.MILLIS);
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);

            return true;
        } catch (Exception e) {
            log.debug("Invalid JWT token: {}", e.getMessage());

            return false;
        }
    }

    public String generateToken(String subject) {
        return createToken(subject, jwtProperties.getExpiration());
    }

    public String generateRefreshToken(String subject) {
        return createToken(subject, jwtProperties.getRefreshExpiration());
    }

    private String createToken(String subject, Long expirationMs) {
        Date now = new Date();

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}