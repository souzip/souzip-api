package com.souzip.adapter.security.admin.jwt;

import com.souzip.application.admin.required.TokenProvider;
import com.souzip.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtTokenProviderAdapter implements TokenProvider {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String generateAccessToken(String subject) {
        return jwtTokenProvider.generateToken(subject);
    }

    @Override
    public String generateRefreshToken(String subject) {
        return jwtTokenProvider.generateRefreshToken(subject);
    }
}