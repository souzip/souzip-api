package com.souzip.domain.admin.presentation.response;

import com.souzip.domain.admin.application.AdminManagementService.AdminPageResult;
import com.souzip.domain.admin.model.Admin;
import com.souzip.domain.admin.model.AdminRole;
import com.souzip.shared.common.dto.pagination.PaginationResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AdminResponse(
        UUID id,
        String username,
        AdminRole role,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
    public static AdminResponse from(Admin admin) {
        return new AdminResponse(
                admin.getId(),
                admin.getUsername().value(),
                admin.getRole(),
                admin.getLastLoginAt(),
                admin.getCreatedAt()
        );
    }

    public static PaginationResponse<AdminResponse> ofPageResult(AdminPageResult result) {
        List<AdminResponse> content = result.admins().stream()
                .map(AdminResponse::from)
                .toList();

        return PaginationResponse.of(
                content,
                result.pageNo(),
                result.pageSize(),
                result.total(),
                result.totalPages()
        );
    }
}
