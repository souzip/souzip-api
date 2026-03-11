package com.souzip.adapter.webapi.admin;

import com.souzip.adapter.security.admin.annotation.AdminAccess;
import com.souzip.adapter.security.admin.annotation.ViewerAccess;
import com.souzip.adapter.webapi.admin.dto.CityResponse;
import com.souzip.adapter.webapi.admin.dto.CountryResponse;
import com.souzip.application.admin.provided.AdminLocationFinder;
import com.souzip.application.admin.provided.AdminLocationModifier;
import com.souzip.domain.city.entity.City;
import com.souzip.domain.city.entity.CityCreateRequest;
import com.souzip.domain.city.entity.CityUpdateRequest;
import com.souzip.global.common.dto.SuccessResponse;
import com.souzip.global.common.dto.pagination.PaginationRequest;
import com.souzip.global.common.dto.pagination.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class AdminLocationApi {

    private final AdminLocationFinder adminLocationFinder;
    private final AdminLocationModifier adminLocationModifier;

    @ViewerAccess
    @GetMapping("/countries")
    public SuccessResponse<List<CountryResponse>> getCountries(
            @RequestParam(required = false) String keyword
    ) {
        return SuccessResponse.of(
                adminLocationFinder.getCountries(keyword).stream()
                        .map(CountryResponse::from)
                        .toList()
        );
    }

    @ViewerAccess
    @GetMapping("/cities")
    public SuccessResponse<PaginationResponse<CityResponse>> getCities(
            @RequestParam Long countryId,
            @RequestParam(required = false) String keyword,
            @ModelAttribute PaginationRequest paginationRequest
    ) {
        Page<City> page = adminLocationFinder.getCities(countryId, keyword, paginationRequest.toPageable());

        List<CityResponse> cities = page.getContent().stream()
                .map(CityResponse::from)
                .toList();

        return SuccessResponse.of(PaginationResponse.of(page, cities));
    }

    @AdminAccess
    @PostMapping("/cities")
    public SuccessResponse<Void> createCity(@Valid @RequestBody CityCreateRequest request) {
        adminLocationModifier.createCity(request);

        return SuccessResponse.of("도시가 추가되었습니다.");
    }

    @AdminAccess
    @PatchMapping("/cities/{cityId}")
    public SuccessResponse<Void> updateCity(
            @PathVariable Long cityId,
            @Valid @RequestBody CityUpdateRequest request
    ) {
        adminLocationModifier.updateCity(cityId, request);

        return SuccessResponse.of("도시 정보가 수정되었습니다.");
    }

    @AdminAccess
    @DeleteMapping("/cities/{cityId}")
    public SuccessResponse<Void> deleteCity(@PathVariable Long cityId) {
        adminLocationModifier.deleteCity(cityId);

        return SuccessResponse.of("도시가 삭제되었습니다.");
    }

    @AdminAccess
    @PatchMapping("/cities/{cityId}/priority")
    public SuccessResponse<Void> updateCityPriority(
            @PathVariable Long cityId,
            @RequestParam(required = false) Integer priority
    ) {
        adminLocationModifier.updateCityPriority(cityId, priority);
        return SuccessResponse.of("우선순위가 업데이트되었습니다.");
    }
}