package com.souzip.api.domain.product.service;

import com.souzip.api.domain.product.dto.ProductCreateRequestDto;
import com.souzip.api.domain.product.dto.ProductResponseDto;
import com.souzip.api.domain.product.entity.Product;
import com.souzip.api.domain.product.repository.ProductRepository;
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
}
