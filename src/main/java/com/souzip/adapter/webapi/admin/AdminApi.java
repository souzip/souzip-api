package com.souzip.adapter.webapi.admin;

import com.souzip.adapter.security.admin.annotation.CurrentAdminId;
import com.souzip.adapter.security.admin.annotation.SuperAdminOnly;
import com.souzip.adapter.webapi.admin.dto.AdminRegisterResponse;
import com.souzip.adapter.webapi.admin.dto.AdminResponse;
import com.souzip.application.admin.provided.AdminFinder;
import com.souzip.application.admin.provided.AdminModifier;
import com.souzip.domain.admin.Admin;
import com.souzip.domain.admin.AdminRegisterRequest;
import com.souzip.global.common.dto.SuccessResponse;
import com.souzip.global.common.dto.pagination.PaginationRequest;
import com.souzip.global.common.dto.pagination.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class AdminApi {

    private final AdminFinder adminFinder;
    private final AdminModifier adminModifier;

    @SuperAdminOnly
    @PostMapping("/register")
    public SuccessResponse<AdminRegisterResponse> register(@Valid @RequestBody AdminRegisterRequest request) {
        return SuccessResponse.of(
                AdminRegisterResponse.from(adminModifier.register(request)),
                "관리자 등록이 완료되었습니다."
        );
    }

    @SuperAdminOnly
    @GetMapping
    public SuccessResponse<PaginationResponse<AdminResponse>> getAdmins(
            @ModelAttribute PaginationRequest paginationRequest
    ) {
        Page<Admin> page = adminFinder.findAll(paginationRequest.toPageable());

        List<AdminResponse> admins = page.getContent().stream()
                .map(AdminResponse::from)
                .toList();

        return SuccessResponse.of(PaginationResponse.of(page, admins));
    }

    @SuperAdminOnly
    @DeleteMapping("/{adminId}")
    public SuccessResponse<Void> deleteAdmin(
            @PathVariable UUID adminId,
            @CurrentAdminId UUID requesterId
    ) {
        adminModifier.delete(adminId, requesterId);

        return SuccessResponse.of("관리자가 삭제되었습니다.");
    }
}