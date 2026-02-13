package com.souzip.api.domain.admin.presentation;

import com.souzip.api.domain.admin.application.AdminAuthService;
import com.souzip.api.domain.admin.application.AdminAuthService.AdminLoginResult;
import com.souzip.api.domain.admin.application.AdminAuthService.RefreshResult;
import com.souzip.api.domain.admin.presentation.request.AdminLoginRequest;
import com.souzip.api.domain.admin.presentation.request.AdminRefreshRequest;
import com.souzip.api.domain.admin.presentation.response.AdminLoginResponse;
import com.souzip.api.domain.admin.presentation.response.AdminRefreshResponse;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.security.annotation.CurrentAdminId;
import com.souzip.api.global.security.annotation.RequireAuth;
import com.souzip.api.global.security.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
@RestController
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public SuccessResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResult result = adminAuthService.login(request.toCommand());
        return SuccessResponse.of(AdminLoginResponse.from(result));
    }

    @PostMapping("/refresh")
    public SuccessResponse<AdminRefreshResponse> refresh(@Valid @RequestBody AdminRefreshRequest request) {
        RefreshResult result = adminAuthService.refresh(request.refreshToken());
        return SuccessResponse.of(AdminRefreshResponse.from(result));
    }

    @PostMapping("/logout")
    @RequireAuth
    public SuccessResponse<Void> logout(@CurrentAdminId UUID adminId) {
        adminAuthService.logout(adminId);
        return SuccessResponse.of(null, "로그아웃되었습니다.");
    }
}
