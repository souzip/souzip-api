package com.souzip.api.domain.admin.presentation;

import com.souzip.api.domain.admin.application.AdminCityQueryUseCase;
import com.souzip.api.domain.admin.application.AdminCountryQueryUseCase;
import com.souzip.api.domain.admin.application.AdminManagementUseCase;
import com.souzip.api.domain.admin.application.command.AdminCreateCityCommand;
import com.souzip.api.domain.admin.application.command.AdminDeleteCityCommand;
import com.souzip.api.domain.admin.application.command.AdminUpdateCityPriorityCommand;
import com.souzip.api.domain.admin.application.command.InviteAdminCommand;
import com.souzip.api.domain.admin.application.port.CityQueryPort.CityQueryResult;
import com.souzip.api.domain.admin.application.port.CountryQueryPort.CountryQueryResult;
import com.souzip.api.domain.admin.application.query.CitySearchQuery;
import com.souzip.api.domain.admin.infrastructure.security.annotation.AdminAccess;
import com.souzip.api.domain.admin.infrastructure.security.annotation.CurrentAdminId;
import com.souzip.api.domain.admin.infrastructure.security.annotation.SuperAdminOnly;
import com.souzip.api.domain.admin.infrastructure.security.annotation.ViewerAccess;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.presentation.request.CreateCityRequest;
import com.souzip.api.domain.admin.presentation.request.InviteAdminRequest;
import com.souzip.api.domain.admin.presentation.response.AdminResponse;
import com.souzip.api.domain.admin.presentation.response.InviteAdminResponse;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.common.dto.pagination.PaginationRequest;
import com.souzip.api.global.common.dto.pagination.PaginationResponse;
import jakarta.validation.Valid;
import java.util.List;
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

    private final AdminManagementUseCase adminManagementUseCase;
    private final AdminCityQueryUseCase adminCityQueryUseCase;
    private final AdminCountryQueryUseCase adminCountryQueryUseCase;

    @SuperAdminOnly
    @PostMapping("/invite")
    public SuccessResponse<InviteAdminResponse> inviteAdmin(
        @Valid @RequestBody InviteAdminRequest request
    ) {
        Admin admin = adminManagementUseCase.inviteAdmin(new InviteAdminCommand(
            request.username(),
            request.password(),
            request.role()
        ));
        return SuccessResponse.of(InviteAdminResponse.from(admin), "관리자 초대가 완료되었습니다.");
    }

    @SuperAdminOnly
    @GetMapping("/list")
    public SuccessResponse<PaginationResponse<AdminResponse>> getAdmins(
        @ModelAttribute PaginationRequest paginationRequest
    ) {
        return SuccessResponse.of(AdminResponse.ofPageResult(
            adminManagementUseCase.getAdmins(
                paginationRequest.getPageNo(),
                paginationRequest.getPageSize()
            )
        ));
    }

    @SuperAdminOnly
    @DeleteMapping("/{adminId}")
    public SuccessResponse<Void> deleteAdmin(
        @PathVariable UUID adminId,
        @CurrentAdminId UUID requesterId
    ) {
        adminManagementUseCase.deleteAdmin(adminId, requesterId);
        return SuccessResponse.of(null, "관리자가 삭제되었습니다.");
    }

    @ViewerAccess
    @GetMapping("/countries")
    public SuccessResponse<List<CountryQueryResult>> getCountries() {
        return SuccessResponse.of(adminCountryQueryUseCase.getCountries());
    }

    @ViewerAccess
    @GetMapping("/cities")
    public SuccessResponse<PaginationResponse<CityQueryResult>> getCities(
        @RequestParam Long countryId,
        @RequestParam(required = false) String keyword,
        @ModelAttribute PaginationRequest paginationRequest
    ) {
        CitySearchQuery query = CitySearchQuery.of(
            countryId,
            keyword,
            paginationRequest.getPageNo(),
            paginationRequest.getPageSize()
        );
        return SuccessResponse.of(adminCityQueryUseCase.getCities(query));
    }

    @AdminAccess
    @PostMapping("/cities")
    public SuccessResponse<Void> createCity(
        @Valid @RequestBody CreateCityRequest request
    ) {
        adminManagementUseCase.createCity(new AdminCreateCityCommand(
            request.nameEn(),
            request.nameKr(),
            request.latitude(),
            request.longitude(),
            request.countryId()
        ));
        return SuccessResponse.of(null, "도시가 추가되었습니다.");
    }

    @AdminAccess
    @DeleteMapping("/cities/{cityId}")
    public SuccessResponse<Void> deleteCity(
        @PathVariable Long cityId
    ) {
        adminManagementUseCase.deleteCity(new AdminDeleteCityCommand(cityId));
        return SuccessResponse.of(null, "도시가 삭제되었습니다.");
    }

    @AdminAccess
    @PatchMapping("/cities/{cityId}/priority")
    public SuccessResponse<Void> updateCityPriority(
        @PathVariable Long cityId,
        @RequestParam(required = false) Integer priority
    ) {
        adminManagementUseCase.updateCityPriority(new AdminUpdateCityPriorityCommand(cityId, priority));
        return SuccessResponse.of(null, "우선순위가 업데이트되었습니다.");
    }
}
