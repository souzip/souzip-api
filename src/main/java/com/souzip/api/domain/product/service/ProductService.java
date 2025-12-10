package com.souzip.api.domain.product.service;

import com.souzip.api.domain.product.dto.ProductCreateRequestDto;
import com.souzip.api.domain.product.dto.ProductResponseDto;
import com.souzip.api.domain.product.dto.ProductUpdateRequestDto;
import com.souzip.api.domain.product.entity.Product;
import com.souzip.api.domain.product.repository.ProductRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequestDto request) {
        Product product = Product.of(
                request.name(),
                request.price(),
                request.imageUrl(),
                request.description(),
                request.category(),
                request.purpose(),
                request.location(),
                request.address()
        );

        Product saved = productRepository.save(product);

        return ProductResponseDto.fromEntity(saved);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductUpdateRequestDto request) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.update(
                request.name(),
                request.price(),
                request.imageUrl(),
                request.description(),
                request.category(),
                request.purpose(),
                request.location(),
                request.address()
        );

        return ProductResponseDto.fromEntity(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.delete();
    }
}
