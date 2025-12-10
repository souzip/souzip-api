package com.souzip.api.domain.product.controller;

import com.souzip.api.domain.product.dto.ProductCreateRequestDto;
import com.souzip.api.domain.product.dto.ProductResponseDto;
import com.souzip.api.domain.product.dto.ProductUpdateRequestDto;
import com.souzip.api.domain.product.service.ProductService;
import com.souzip.api.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public SuccessResponse<ProductResponseDto> createProduct(@RequestBody ProductCreateRequestDto request) {
        return SuccessResponse.of(productService.createProduct(request), "기념품이 성공적으로 등록되었습니다.");
    }

    @PutMapping("/{id}")
    public SuccessResponse<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequestDto request
    ) {
        return SuccessResponse.of(productService.updateProduct(id, request), "기념품이 성공적으로 수정되었습니다.");
    }
}
