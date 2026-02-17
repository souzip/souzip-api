package com.souzip.api.domain.admin.presentation;

import com.souzip.api.domain.admin.application.AdminManagementService;
import com.souzip.api.domain.admin.infrastructure.security.annotation.AdminAccess;
import com.souzip.api.domain.admin.infrastructure.security.annotation.CurrentAdminId;
import com.souzip.api.domain.admin.infrastructure.security.annotation.SuperAdminOnly;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.presentation.request.InviteAdminRequest;
import com.souzip.api.domain.admin.presentation.response.AdminResponse;
import com.souzip.api.domain.admin.presentation.response.InviteAdminResponse;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.common.dto.pagination.PaginationRequest;
import com.souzip.api.global.common.dto.pagination.PaginationResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    @SuperAdminOnly
    @PostMapping("/invite")
    public SuccessResponse<InviteAdminResponse> inviteAdmin(
        @Valid @RequestBody InviteAdminRequest request
    ) {
        Admin admin = adminManagementService.inviteAdmin(request.toCommand());
        return SuccessResponse.of(InviteAdminResponse.from(admin), "관리자 초대가 완료되었습니다.");
    }

    @SuperAdminOnly
    @GetMapping("/list")
    public SuccessResponse<PaginationResponse<AdminResponse>> getAdmins(
        @ModelAttribute PaginationRequest paginationRequest
    ) {
        return SuccessResponse.of(AdminResponse.ofPageResult(adminManagementService.getAdmins(
            paginationRequest.getPageNo(),
            paginationRequest.getPageSize()
        )));
    }

    @SuperAdminOnly
    @DeleteMapping("/{adminId}")
    public SuccessResponse<Void> deleteAdmin(@PathVariable UUID adminId, @CurrentAdminId UUID requesterId) {
        adminManagementService.deleteAdmin(adminId, requesterId);
        return SuccessResponse.of(null, "관리자가 삭제되었습니다.");
    }

    @AdminAccess
    @PatchMapping("/cities/{cityId}/priority")
    public SuccessResponse<Void> updateCityPriority(
        @PathVariable Long cityId,
        @RequestParam(required = false) Integer priority
    ) {
        adminManagementService.updateCityPriority(cityId, priority);
        return SuccessResponse.of(null, "우선순위가 업데이트되었습니다.");
    }
}
