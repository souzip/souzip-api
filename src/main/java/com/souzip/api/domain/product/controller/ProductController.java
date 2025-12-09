package com.souzip.api.domain.product.controller;

import com.souzip.api.domain.product.dto.ProductCreateRequestDto;
import com.souzip.api.domain.product.dto.ProductResponseDto;
import com.souzip.api.domain.product.service.ProductService;
import com.souzip.api.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public SuccessResponse<ProductResponseDto> createProduct(@RequestBody ProductCreateRequestDto request) {
        return SuccessResponse.of(productService.createProduct(request), "기념품이 성공적으로 등록되었습니다.");
    }
}
