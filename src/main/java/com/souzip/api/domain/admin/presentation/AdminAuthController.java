package com.souzip.api.domain.admin.presentation;

import com.souzip.api.domain.admin.application.AdminAuthService;
import com.souzip.api.domain.admin.application.AdminAuthService.AdminLoginResult;
import com.souzip.api.domain.admin.presentation.request.AdminLoginRequest;
import com.souzip.api.domain.admin.presentation.response.AdminLoginResponse;
import com.souzip.api.global.common.dto.SuccessResponse;
import jakarta.validation.Valid;
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
}
