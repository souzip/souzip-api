package com.souzip.api.domain.souvenir.controller;

import com.souzip.api.domain.souvenir.dto.SouvenirCreateRequestDto;
import com.souzip.api.domain.souvenir.dto.SouvenirResponseDto;
import com.souzip.api.domain.souvenir.dto.SouvenirUpdateRequestDto;
import com.souzip.api.domain.souvenir.service.SouvenirService;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.security.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class SouvenirController {

    private final SouvenirService souvenirService;

    @GetMapping("/{id}")
    public SuccessResponse<SouvenirResponseDto> getProduct(@PathVariable Long id) {
        SouvenirResponseDto response = souvenirService.getProduct(id);
        return SuccessResponse.of(response, "기념품이 성공적으로 조회되었습니다.");
    }

    @PostMapping
    public SuccessResponse<SouvenirResponseDto> createProduct(
            @RequestPart("product") SouvenirCreateRequestDto request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @CurrentUserId Long userId
    ) {
        SouvenirResponseDto response = souvenirService.createProduct(request, userId, files);
        return SuccessResponse.of(response, "기념품이 성공적으로 등록되었습니다.");
    }

    @PutMapping("/{id}")
    public SuccessResponse<SouvenirResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") SouvenirUpdateRequestDto request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @CurrentUserId Long userId
    ) {
        SouvenirResponseDto response = souvenirService.updateProduct(id, request, userId, files);
        return SuccessResponse.of(response, "기념품이 성공적으로 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<Void> deleteProduct(
            @PathVariable Long id,
            @CurrentUserId Long userId
    ) {
        souvenirService.deleteProduct(id, userId);
        return SuccessResponse.of(null, "기념품이 성공적으로 삭제되었습니다.");
    }
}
