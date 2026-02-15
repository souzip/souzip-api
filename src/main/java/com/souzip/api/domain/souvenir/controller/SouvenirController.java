package com.souzip.api.domain.souvenir.controller;

import com.souzip.api.domain.souvenir.dto.*;
import com.souzip.api.domain.souvenir.service.SouvenirService;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.security.annotation.CurrentUserId;
import com.souzip.api.global.security.annotation.RequireAuth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SouvenirController {

    private final SouvenirService souvenirService;

    @GetMapping("/api/souvenirs/nearby")
    public SuccessResponse<SouvenirNearbyListResponse> getNearbySouvenirs(
        @RequestParam double latitude,
        @RequestParam double longitude,
        @RequestParam(required = false, defaultValue = "5000") double radiusMeter
    ) {
        return SuccessResponse.of(souvenirService.getNearbySouvenirs(latitude, longitude, radiusMeter));
    }

    @GetMapping("/api/souvenirs/{id}")
    public SuccessResponse<SouvenirDetailResponse> getSouvenir(
        @PathVariable Long id,
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        SouvenirDetailResponse response = souvenirService.getSouvenir(id, authorizationHeader);
        return SuccessResponse.of(response);
    }

    @RequireAuth
    @DeleteMapping("/api/souvenirs/{id}")
    public SuccessResponse<Void> deleteSouvenir(@PathVariable Long id, @CurrentUserId Long userId) {
        souvenirService.deleteSouvenir(id, userId);
        return SuccessResponse.of(null, "기념품이 성공적으로 삭제되었습니다.");
    }

    @RequireAuth
    @PostMapping("/api/souvenirs")
    public SuccessResponse<SouvenirResponse> createSouvenir(
        @Valid @RequestPart("souvenir") SouvenirCreateRequest request,
        @RequestPart(value = "files") List<MultipartFile> files,
        @CurrentUserId Long userId
    ) {
        SouvenirResponse response = souvenirService.createSouvenir(request, userId, files);
        return SuccessResponse.of(response);
    }

    @RequireAuth
    @PutMapping("/api/souvenirs/{id}")
    public SuccessResponse<SouvenirResponse> updateSouvenir(
        @Valid @PathVariable Long id,
        @RequestPart("souvenir") SouvenirUpdateRequest request,
        @CurrentUserId Long userId
    ) {
        SouvenirResponse response = souvenirService.updateSouvenir(id, request, userId);
        return SuccessResponse.of(response);
    }

    @RequireAuth
    @PostMapping("/api/v2/souvenirs")
    public SuccessResponse<SouvenirResponse> createSouvenirV2(
        @Valid @RequestPart("souvenir") SouvenirRequest request,
        @RequestPart(value = "files") List<MultipartFile> files,
        @CurrentUserId Long userId
    ) {
        SouvenirResponse response = souvenirService.createSouvenirV2(request, userId, files);
        return SuccessResponse.of(response);
    }

    @RequireAuth
    @PutMapping("/api/v2/souvenirs/{id}")
    public SuccessResponse<SouvenirResponse> updateSouvenirV2(
        @PathVariable Long id,
        @Valid @RequestPart("souvenir") SouvenirRequest request,
        @CurrentUserId Long userId
    ) {
        SouvenirResponse response = souvenirService.updateSouvenirV2(id, request, userId);
        return SuccessResponse.of(response);
    }
}
