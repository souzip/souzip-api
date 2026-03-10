package com.souzip.adapter.webapi.admin;

import com.souzip.adapter.security.admin.annotation.CurrentAdminId;
import com.souzip.adapter.webapi.admin.dto.AdminLoginRequest;
import com.souzip.adapter.webapi.admin.dto.AdminLoginResponse;
import com.souzip.adapter.webapi.admin.dto.AdminRefreshRequest;
import com.souzip.adapter.webapi.admin.dto.AdminRefreshResponse;
import com.souzip.application.admin.AdminAuthService;
import com.souzip.global.common.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
@RestController
public class AdminAuthApi {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public SuccessResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return SuccessResponse.of(
                AdminLoginResponse.from(adminAuthService.login(request.username(), request.password()))
        );
    }

    @PostMapping("/refresh")
    public SuccessResponse<AdminRefreshResponse> refresh(@Valid @RequestBody AdminRefreshRequest request) {
        return SuccessResponse.of(
                AdminRefreshResponse.from(adminAuthService.refresh(request.refreshToken()))
        );
    }

    @PostMapping("/logout")
    public SuccessResponse<Void> logout(@CurrentAdminId UUID adminId) {
        adminAuthService.logout(adminId);

        return SuccessResponse.of(null, "로그아웃되었습니다.");
    }
}