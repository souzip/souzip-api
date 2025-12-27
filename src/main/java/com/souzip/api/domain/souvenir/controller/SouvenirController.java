package com.souzip.api.domain.souvenir.controller;

import com.souzip.api.domain.souvenir.dto.*;
import com.souzip.api.domain.souvenir.service.SouvenirService;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.security.annotation.CurrentUserId;
import com.souzip.api.global.security.annotation.RequireAuth;
import io.micrometer.common.lang.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/souvenirs")
@RequiredArgsConstructor
public class SouvenirController {

    private final SouvenirService souvenirService;

    @GetMapping("/nearby")
    public SuccessResponse<SouvenirNearbyListResponse> getNearbySouvenirs(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        return SuccessResponse.of(souvenirService.getNearbySouvenirs(latitude, longitude));
    }

    @GetMapping("/{id}")
    public SuccessResponse<SouvenirResponse> getSouvenir(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        SouvenirResponse response = souvenirService.getSouvenir(id, authorizationHeader);
        return SuccessResponse.of(response);
    }

    @RequireAuth
    @PostMapping
    public SuccessResponse<SouvenirResponse> createSouvenir(
            @Valid
            @RequestPart("souvenir") SouvenirCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @CurrentUserId Long userId
    ) {
        SouvenirResponse response = souvenirService.createSouvenir(request, userId, files);
        return SuccessResponse.of(response);
    }

    @RequireAuth
    @PutMapping("/{id}")
    public SuccessResponse<SouvenirResponse> updateSouvenir(
            @Valid
            @PathVariable Long id,
            @RequestPart("souvenir") SouvenirUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @CurrentUserId Long userId
    ) {
        SouvenirResponse response = souvenirService.updateSouvenir(id, request, userId, files);
        return SuccessResponse.of(response);
    }

    @RequireAuth
    @DeleteMapping("/{id}")
    public SuccessResponse<Void> deleteSouvenir(
            @PathVariable Long id,
            @CurrentUserId Long userId
    ) {
        souvenirService.deleteSouvenir(id, userId);
        return SuccessResponse.of(null, "기념품이 성공적으로 삭제되었습니다.");
    }
}
