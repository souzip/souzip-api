package com.souzip.api.domain.product.controller;

import com.souzip.api.domain.product.dto.ProductCreateRequestDto;
import com.souzip.api.domain.product.dto.ProductResponseDto;
import com.souzip.api.domain.product.dto.ProductUpdateRequestDto;
import com.souzip.api.domain.product.service.ProductService;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.security.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public SuccessResponse<ProductResponseDto> getProduct(@PathVariable Long id) {
        ProductResponseDto response = productService.getProduct(id);
        return SuccessResponse.of(response, "기념품이 성공적으로 조회되었습니다.");
    }

    @PostMapping
    public SuccessResponse<ProductResponseDto> createProduct(
            @RequestPart("product") ProductCreateRequestDto request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @CurrentUserId Long userId
    ) {
        ProductResponseDto response = productService.createProduct(request, userId, files);
        return SuccessResponse.of(response, "기념품이 성공적으로 등록되었습니다.");
    }

    @PutMapping("/{id}")
    public SuccessResponse<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") ProductUpdateRequestDto request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @CurrentUserId Long userId
    ) {
        ProductResponseDto response = productService.updateProduct(id, request, userId, files);
        return SuccessResponse.of(response, "기념품이 성공적으로 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public SuccessResponse<Void> deleteProduct(
            @PathVariable Long id,
            @CurrentUserId Long userId
    ) {
        productService.deleteProduct(id, userId);
        return SuccessResponse.of(null, "기념품이 성공적으로 삭제되었습니다.");
    }
}
