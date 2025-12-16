package com.souzip.api.domain.souvenir.service;

import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.file.service.FileService;
import com.souzip.api.domain.souvenir.dto.SouvenirCreateRequest;
import com.souzip.api.domain.souvenir.dto.SouvenirResponse;
import com.souzip.api.domain.souvenir.dto.SouvenirUpdateRequest;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import com.souzip.api.domain.souvenir.repository.SouvenirRepository;
import com.souzip.api.domain.user.repository.UserRepository;
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
public class SouvenirService {

    private final SouvenirRepository souvenirRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    public SouvenirResponse getProduct(Long productId) {
        Souvenir souvenir = souvenirRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        List<FileResponse> files = fileService.getFilesByEntity("Product", productId);
        return SouvenirResponse.from(souvenir, files);
    }

    @Transactional
    public SouvenirResponse createProduct(SouvenirCreateRequest request, Long userId, List<MultipartFile> files) {

        Souvenir souvenir = Souvenir.of(
                request.name(),
                request.price(),
                request.description(),
                request.category(),
                request.purpose(),
                request.cityId(),
                userId
        );

        Souvenir savedProduct = souvenirRepository.save(souvenir);
        List<FileResponse> uploadedFiles = uploadFiles(savedProduct.getId(), userId, files);
        return SouvenirResponse.from(savedProduct, uploadedFiles);
    }

    @Transactional
    public SouvenirResponse updateProduct(Long id, SouvenirUpdateRequest request, Long userId, List<MultipartFile> files) {
        Souvenir product = souvenirRepository.findByIdAndDeletedFalse(id)
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
        return SouvenirResponse.from(product, uploadedFiles);
    }

    @Transactional
    public void deleteProduct(Long id, Long userId) {
        Souvenir product = souvenirRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        fileService.deleteFilesByEntity("Product", id);
        product.delete();
    }

    private List<FileResponse> uploadFiles(Long productId, Long userId, List<MultipartFile> files) {
        String uuid = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();

        return Optional.ofNullable(files)
                .orElse(List.of())
            .stream()
                .map(file -> fileService.uploadFile(
                        uuid,
                        "Product",
                        productId,
                        file,
                        null
                ))
                .toList();
    }
}
