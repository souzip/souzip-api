// src/main/java/com/souzip/api/domain/admin/presentation/AdminManagementController.java
package com.souzip.api.domain.admin.presentation;

import com.souzip.api.domain.admin.application.AdminManagementService;
import com.souzip.api.domain.admin.infrastructure.security.annotation.SuperAdminOnly;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.presentation.request.InviteAdminRequest;
import com.souzip.api.domain.admin.presentation.response.InviteAdminResponse;
import com.souzip.api.global.common.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/admin/")
@RestController
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    @SuperAdminOnly
    @PostMapping("/invite")
    public SuccessResponse<InviteAdminResponse> inviteAdmin(@Valid @RequestBody InviteAdminRequest request) {
        Admin admin = adminManagementService.inviteAdmin(request.toCommand());
        return SuccessResponse.of(InviteAdminResponse.from(admin), "관리자 초대가 완료되었습니다.");
    }
}
