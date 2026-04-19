package com.souzip.auth.adapter.web;

import com.souzip.auth.adapter.security.annotation.CurrentUserId;
import com.souzip.auth.adapter.security.annotation.RequireAuth;
import com.souzip.auth.adapter.web.dto.LoginRequest;
import com.souzip.auth.adapter.web.dto.LoginResponse;
import com.souzip.auth.adapter.web.dto.RefreshRequest;
import com.souzip.auth.adapter.web.dto.RefreshResponse;
import com.souzip.auth.application.dto.LoginInfo;
import com.souzip.auth.application.dto.RefreshInfo;
import com.souzip.auth.application.provided.Auth;
import com.souzip.shared.common.dto.SuccessResponse;
import com.souzip.shared.domain.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthApi {

    private final Auth auth;

    @PostMapping("/login/{provider}")
    public SuccessResponse<LoginResponse> login(
            @PathVariable String provider,
            @RequestBody LoginRequest request
    ) {
        LoginInfo info = auth.login(Provider.from(provider), request.accessToken());

        return SuccessResponse.of(LoginResponse.from(info));
    }

    @PostMapping("/refresh")
    public SuccessResponse<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        RefreshInfo info = auth.refresh(request.refreshToken());

        return SuccessResponse.of(RefreshResponse.from(info));
    }

    @RequireAuth
    @PostMapping("/logout")
    public SuccessResponse<Void> logout(@CurrentUserId Long currentUserId) {
        auth.logout(currentUserId);

        return SuccessResponse.of(null, "로그아웃 되었습니다.");
    }
}