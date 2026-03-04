package com.souzip.domain.auth.controller;

import com.souzip.domain.auth.dto.LoginRequest;
import com.souzip.domain.auth.dto.LoginResponse;
import com.souzip.domain.auth.dto.RefreshRequest;
import com.souzip.domain.auth.dto.RefreshResponse;
import com.souzip.domain.auth.service.AuthService;
import com.souzip.domain.user.entity.Provider;
import com.souzip.global.common.dto.SuccessResponse;
import com.souzip.global.security.annotation.CurrentUserId;
import com.souzip.global.security.annotation.RequireAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/{provider}")
    public SuccessResponse<LoginResponse> login(@PathVariable String provider, @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(Provider.from(provider), request.accessToken());
        return SuccessResponse.of(response);
    }

    @PostMapping("/refresh")
    public SuccessResponse<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        RefreshResponse response = authService.refresh(request.refreshToken());
        return SuccessResponse.of(response);
    }

    @PostMapping("/logout")
    @RequireAuth
    public SuccessResponse<Void> logout(@CurrentUserId Long currentUserId) {
        authService.logout(currentUserId);
        return SuccessResponse.of(null, "로그아웃 되었습니다.");
    }
}
