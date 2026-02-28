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
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SouvenirService {

    public static final String ENTITY_TYPE_SOUVENIR = "Souvenir";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    private final SouvenirRepository souvenirRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ExchangeRateService exchangeRateService;
    private final FileStorageService fileStorageService;
    private final JwtTokenProvider jwtTokenProvider;

    public SouvenirNearbyListResponse getNearbySouvenirs(
        double latitude,
        double longitude,
        double radiusMeter
    ) {
        List<Object[]> results = souvenirRepository.findNearbySouvenirs(
            latitude,
            longitude,
            radiusMeter
        );

        List<SouvenirNearbyResponse> list = results.stream()
            .map(row -> SouvenirNearbyResponse.fromObjectArray(
                row,
                fileStorageService::generatePresignedUrl
            ))
            .toList();

        return SouvenirNearbyListResponse.from(list);
    }

    public SouvenirDetailResponse getSouvenir(
        Long souvenirId,
        @Nullable String authorizationHeader
    ) {
        String userId = extractUserId(authorizationHeader);

        Souvenir souvenir = findSouvenirById(souvenirId);
        List<FileResponse> files = getFiles(souvenirId);
        boolean isOwned = souvenir.isOwnedBy(userId);
        PriceResponse priceResponse = createPriceResponse(souvenir);

        return SouvenirDetailResponse.of(souvenir, files, isOwned, priceResponse);
    }

    @Audit(action = AuditAction.SOUVENIR_DELETED)
    @Transactional
    public void deleteSouvenir(Long id, Long userId) {
        requireUserId(userId);

        Souvenir souvenir = findSouvenirWithOwnershipCheck(id, userId);

        fileService.deleteFilesByEntity(ENTITY_TYPE_SOUVENIR, id);
        souvenir.delete();
    }

    @Audit(action = AuditAction.SOUVENIR_CREATED)
    @Transactional
    public SouvenirResponse createSouvenir(
        SouvenirCreateRequest request,
        Long userId,
        List<MultipartFile> files
    ) {
        requireUserId(userId);
        User user = findUserById(userId);

        ExchangeCalculatedPrice price = exchangeRateService.calculatePrice(
            request.countryCode(),
            request.localPrice(),
            request.krwPrice()
        );

        Souvenir souvenir = Souvenir.of(
            request,
            user,
            price.localPrice(),
            price.krwPrice()
        );

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

        ExchangeCalculatedPrice price = exchangeRateService.calculatePrice(
            request.countryCode(),
            request.localPrice(),
            request.krwPrice()
        );

        souvenir.update(request, price.localPrice(), price.krwPrice());

        return SouvenirResponse.of(souvenir, List.of());
    }

    @Audit(action = AuditAction.SOUVENIR_CREATED)
    @Transactional
    public SouvenirResponse createSouvenirV2(
        SouvenirRequest request,
        Long userId,
        List<MultipartFile> files
    ) {
        requireUserId(userId);
        User user = findUserById(userId);

        PriceData priceData = exchangeRateService.calculatePriceData(
            request.price(),
            request.currency(),
            request.countryCode()
        );

        Souvenir souvenir = Souvenir.ofV2(
            request,
            user,
            priceData.originalPrice(),
            priceData.exchangeAmount(),
            priceData.currencySymbol(),
            priceData.convertedPrice()
        );
        souvenirRepository.save(souvenir);

        List<FileResponse> uploadedFiles = uploadFiles(souvenir.getId(), userId, files);

        PriceResponse priceResponse = createPriceResponse(souvenir);
        return SouvenirResponse.of(souvenir, uploadedFiles, priceResponse);
    }

    @Audit(action = AuditAction.SOUVENIR_UPDATED)
    @Transactional
    public SouvenirResponse updateSouvenirV2(
        Long id,
        SouvenirRequest request,
        Long userId
    ) {
        requireUserId(userId);

        Souvenir souvenir = findSouvenirWithOwnershipCheck(id, userId);

        PriceData priceData = exchangeRateService.calculatePriceData(
            request.price(),
            request.currency(),
            request.countryCode()
        );

        souvenir.updateV2(
            request,
            priceData.originalPrice(),
            priceData.exchangeAmount(),
            priceData.currencySymbol(),
            priceData.convertedPrice()
        );

        PriceResponse priceResponse = createPriceResponse(souvenir);
        return SouvenirResponse.of(souvenir, List.of(), priceResponse);
    }

    private PriceResponse createPriceResponse(Souvenir souvenir) {
        return exchangeRateService.createPriceResponse(
            souvenir.getOriginalPrice(),
            souvenir.getConvertedPrice()
        );
    }

    @Nullable
    private String extractUserId(@Nullable String authorizationHeader) {
        if (hasNoAuthorizationHeader(authorizationHeader)) {
            return null;
        }

        if (isNotBearerToken(authorizationHeader)) {
            return null;
        }

        String token = extractToken(authorizationHeader);

        if (isEmptyToken(token)) {
            return null;
        }

        return parseUserIdFromToken(token);
    }

    private boolean hasNoAuthorizationHeader(String authorizationHeader) {
        return authorizationHeader == null;
    }

    private boolean isNotBearerToken(String authorizationHeader) {
        return !authorizationHeader.startsWith(BEARER_PREFIX);
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.substring(BEARER_PREFIX_LENGTH).trim();
    }

    private boolean isEmptyToken(String token) {
        return token.isEmpty();
    }

    @Nullable
    private String parseUserIdFromToken(String token) {
        try {
            return jwtTokenProvider.getUserIdFromToken(token);
        } catch (Exception e) {
            log.debug("Failed to parse token", e);
            return null;
        }
    }

    private void requireUserId(Long userId) {
        if (isNullUserId(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private boolean isNullUserId(Long userId) {
        return userId == null;
    }

    private Souvenir findSouvenirById(Long souvenirId) {
        return souvenirRepository.findByIdWithUser(souvenirId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SOUVENIR_NOT_FOUND));
    }

    private Souvenir findSouvenirWithOwnershipCheck(Long id, Long userId) {
        Souvenir souvenir = findSouvenirById(id);
        validateOwnership(souvenir, userId);
        return souvenir;
    }

    private void validateOwnership(Souvenir souvenir, Long userId) {
        if (isNotOwner(souvenir, userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean isNotOwner(Souvenir souvenir, Long userId) {
        return !souvenir.getUser().getId().equals(userId);
    }

    private List<FileResponse> getFiles(Long souvenirId) {
        return fileService.getFilesByEntity(ENTITY_TYPE_SOUVENIR, souvenirId);
    }

    private List<FileResponse> uploadFiles(
        Long souvenirId,
        Long userId,
        List<MultipartFile> files
    ) {
        if (hasNoFiles(files)) {
            return Collections.emptyList();
        }

        String userUuid = getUserUuid(userId);

        return files.stream()
            .map(file -> uploadSingleFile(userUuid, souvenirId, file))
            .toList();
    }

    private boolean hasNoFiles(List<MultipartFile> files) {
        return files == null || files.isEmpty();
    }

    private String getUserUuid(Long userId) {
        User user = findUserById(userId);
        return user.getUserId();
    }

    private FileResponse uploadSingleFile(
        String userUuid,
        Long souvenirId,
        MultipartFile file
    ) {
        return fileService.uploadFile(
            userUuid,
            ENTITY_TYPE_SOUVENIR,
            souvenirId,
            file,
            null
        );
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
