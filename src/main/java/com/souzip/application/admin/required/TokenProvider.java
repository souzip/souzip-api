package com.souzip.application.admin.required;

import org.springframework.stereotype.Component;

@Component
public interface TokenProvider {
    String generateAccessToken(String subject);

    String generateRefreshToken(String subject);
}