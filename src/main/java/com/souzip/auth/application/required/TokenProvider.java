package com.souzip.auth.application.required;

import java.time.LocalDateTime;

public interface TokenProvider {
    String generateAccessToken(Long userId);

    String generateRefreshToken();

    LocalDateTime getRefreshTokenExpiresAt();
}