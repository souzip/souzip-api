package com.souzip.api.domain.product.service;

import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.file.service.FileService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final FileService fileService;

    public ProductResponseDto getProduct(Long productId) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        List<FileResponse> files = fileService.getFilesByEntity("Product", productId);
        return ProductResponseDto.from(product, files);
    }

    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequestDto request, Long userId, List<MultipartFile> files) {

        Product product = Product.of(
                request.name(),
                request.price(),
                request.description(),
                request.category(),
                request.purpose(),
                request.cityId(),
                userId
        );

        Product savedProduct = productRepository.save(product);
        List<FileResponse> uploadedFiles = uploadFiles(savedProduct.getId(), userId, files);
        return ProductResponseDto.from(savedProduct, uploadedFiles);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductUpdateRequestDto request, Long userId, List<MultipartFile> files) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        product.update(
                request.name(),
                request.price(),
                request.description(),
                request.category(),
                request.purpose(),
                request.cityId()
        );

        fileService.deleteFilesByEntity("Product", id);
        List<FileResponse> uploadedFiles = uploadFiles(id, userId, files);
        return ProductResponseDto.from(product, uploadedFiles);
    }

    @Transactional
    public void deleteProduct(Long id, Long userId) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        fileService.deleteFilesByEntity("Product", id);
        product.delete();
    }

    private List<FileResponse> uploadFiles(Long productId, Long userId, List<MultipartFile> files) {
        return Optional.ofNullable(files)
                .orElse(List.of())
                .stream()
                .map(file -> fileService.uploadFile(
                        userId.toString(),
                        "Product",
                        productId,
                        file,
                        null
                ))
                .toList();
    }
}
