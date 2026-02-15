package com.souzip.api.domain.souvenir.service;

import com.souzip.api.domain.audit.entity.AuditAction;
import com.souzip.api.domain.exchangerate.dto.ExchangeCalculatedPrice;
import com.souzip.api.domain.exchangerate.service.ExchangeRateService;
import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.file.service.FileService;
import com.souzip.api.domain.file.service.FileStorageService;
import com.souzip.api.domain.souvenir.dto.*;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import com.souzip.api.domain.souvenir.repository.SouvenirRepository;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.domain.user.repository.UserRepository;
import com.souzip.api.global.audit.annotation.Audit;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import com.souzip.api.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SouvenirService {

    public static final String ENTITY_TYPE_SOUVENIR = "Souvenir";
    private final SouvenirRepository souvenirRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ExchangeRateService exchangeRateService;
    private final FileStorageService fileStorageService;
    private final JwtTokenProvider jwtTokenProvider;

    public SouvenirNearbyListResponse getNearbySouvenirs(double latitude, double longitude, double radiusMeter) {
        List<Object[]> results = souvenirRepository.findNearbySouvenirs(latitude, longitude, radiusMeter);

        List<SouvenirNearbyResponse> list = results.stream()
            .map(row -> SouvenirNearbyResponse.fromObjectArray(row, fileStorageService::generatePresignedUrl))
            .toList();

        return SouvenirNearbyListResponse.from(list);
    }

    public SouvenirDetailResponse getSouvenir(Long souvenirId, @Nullable String authorizationHeader) {
        String userId = extractUserId(authorizationHeader);

        Souvenir souvenir = souvenirRepository.findByIdWithUser(souvenirId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SOUVENIR_NOT_FOUND));

        List<FileResponse> files = fileService.getFilesByEntity(ENTITY_TYPE_SOUVENIR, souvenirId);
        boolean isOwned = souvenir.isOwnedBy(userId);

        return SouvenirDetailResponse.of(souvenir, files, isOwned);
    }

    @Audit(action = AuditAction.SOUVENIR_CREATED)
    @Transactional
    public SouvenirResponse createSouvenir(
        SouvenirCreateRequest request,
        Long userId,
        List<MultipartFile> files
    ) {
        requireUserId(userId);
        User user = validateUser(userId);

        ExchangeCalculatedPrice price = calculateExchangePrice(request);
        Souvenir souvenir = Souvenir.of(request, user, price.localPrice(), price.krwPrice());

        souvenirRepository.save(souvenir);
        List<FileResponse> uploadedFiles = uploadFiles(souvenir.getId(), userId, files);

        return SouvenirResponse.of(souvenir, uploadedFiles);
    }

    @Audit(action = AuditAction.SOUVENIR_UPDATED)
    @Transactional
    public SouvenirResponse updateSouvenir(
        Long id,
        SouvenirUpdateRequest request,
        Long userId
    ) {
        requireUserId(userId);

        Souvenir souvenir = findSouvenirWithOwnershipCheck(id, userId);
        ExchangeCalculatedPrice price = calculateExchangePrice(request);

        souvenir.update(request, price.localPrice(), price.krwPrice());

        return SouvenirResponse.of(souvenir, List.of());
    }

    @Audit(action = AuditAction.SOUVENIR_DELETED)
    @Transactional
    public void deleteSouvenir(Long id, Long userId) {
        requireUserId(userId);

        Souvenir souvenir = findSouvenirWithOwnershipCheck(id, userId);

        fileService.deleteFilesByEntity(ENTITY_TYPE_SOUVENIR, id);
        souvenir.delete();
    }

    @Nullable
    private String extractUserId(@Nullable String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            return null;
        }

        try {
            return jwtTokenProvider.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    private ExchangeCalculatedPrice calculateExchangePrice(SouvenirCreateRequest request) {
        return exchangeRateService.calculatePrice(
            request.countryCode(),
            request.localPrice(),
            request.krwPrice()
        );
    }

    private ExchangeCalculatedPrice calculateExchangePrice(SouvenirUpdateRequest request) {
        return exchangeRateService.calculatePrice(
            request.countryCode(),
            request.localPrice(),
            request.krwPrice()
        );
    }

    private Souvenir findSouvenirWithOwnershipCheck(Long id, Long userId) {
        Souvenir souvenir = souvenirRepository.findByIdWithUser(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.SOUVENIR_NOT_FOUND));

        if (!souvenir.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return souvenir;
    }

    private List<FileResponse> uploadFiles(Long souvenirId, Long userId, List<MultipartFile> files) {
        String uuid = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
            .getUserId();

        return Optional.ofNullable(files)
            .orElse(List.of())
            .stream()
            .map(file -> fileService.uploadFile(uuid, ENTITY_TYPE_SOUVENIR, souvenirId, file, null))
            .toList();
    }

    private User validateUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
