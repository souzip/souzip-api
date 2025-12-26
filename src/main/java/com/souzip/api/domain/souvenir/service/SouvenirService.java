package com.souzip.api.domain.souvenir.service;

import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.exchangerate.dto.ExchangeCalculatedPrice;
import com.souzip.api.domain.exchangerate.service.ExchangeRateService;
import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.file.service.FileService;
import com.souzip.api.domain.file.service.FileStorageService;
import com.souzip.api.domain.souvenir.dto.*;
import com.souzip.api.domain.souvenir.entity.Purpose;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import com.souzip.api.domain.souvenir.repository.SouvenirRepository;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.domain.user.repository.UserRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import com.souzip.api.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SouvenirService {

    private static final double NEARBY_RADIUS_METER = 4000;

    private final SouvenirRepository souvenirRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ExchangeRateService exchangeRateService;
    private final FileStorageService fileStorageService;
    private final JwtTokenProvider jwtTokenProvider;

    public List<SouvenirNearbyResponse> getNearbySouvenirs(double latitude, double longitude) {
        List<Object[]> results = souvenirRepository.findNearbySouvenirs(latitude, longitude, NEARBY_RADIUS_METER);

        return results.stream()
                .map(row -> {
                    Long id = ((Number) row[0]).longValue();
                    String name = (String) row[1];
                    Category category = Category.valueOf((String) row[2]);
                    Purpose purpose = Purpose.valueOf((String) row[3]);
                    int localPrice = ((Number) row[4]).intValue();
                    int krwPrice = ((Number) row[5]).intValue();
                    String currencySymbol = (String) row[6];
                    String thumbnail = (String) row[7];
                    double distance = ((Number) row[8]).doubleValue();
                    BigDecimal lat = (BigDecimal) row[9];
                    BigDecimal lon = (BigDecimal) row[10];
                    String address = (String) row[11];

                    String imageUrl = thumbnail != null
                            ? fileStorageService.generatePresignedUrl(thumbnail)
                            : null;

                    return SouvenirNearbyResponse.from(
                            id,
                            name,
                            CategoryDto.from(category),
                            PurposeDto.from(purpose),
                            localPrice,
                            krwPrice,
                            currencySymbol,
                            imageUrl,
                            distance,
                            lat,
                            lon,
                            address
                    );
                })
                .toList();
    }

    public SouvenirResponse getSouvenir(Long souvenirId, @Nullable String authorizationHeader) {
        String userId = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            userId = jwtTokenProvider.getUserIdFromToken(token);
        }

        Souvenir souvenir = souvenirRepository.findByIdAndDeletedFalse(souvenirId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOUVENIR_NOT_FOUND));

        List<FileResponse> files = fileService.getFilesByEntity("Souvenir", souvenirId);

        boolean isOwned = false;
        if (userId != null) {
            isOwned = souvenir.getUser().getUserId().equals(userId);
        }

        return SouvenirResponse.of(souvenir, files, isOwned);
    }

    @Transactional
    public SouvenirResponse createSouvenir(
            SouvenirCreateRequest request,
            Long userId,
            List<MultipartFile> files
    ) {
        requireUserId(userId);
        User user = validateUser(userId);

        ExchangeCalculatedPrice price = exchangeRateService.calculatePrice(
                request.countryCode(),
                request.localPrice(),
                request.krwPrice()
        );

        Souvenir souvenir = Souvenir.of(
                request.name(),
                price.localPrice(),
                request.currencySymbol(),
                price.krwPrice(),
                request.description(),
                request.address(),
                request.locationDetail(),
                request.latitude(),
                request.longitude(),
                request.category(),
                request.purpose(),
                request.countryCode(),
                user,
                true
        );

        souvenirRepository.save(souvenir);
        List<FileResponse> uploadedFiles =
                uploadFiles(souvenir.getId(), userId, files);

        return SouvenirResponse.of(souvenir, uploadedFiles, true);
    }

    @Transactional
    public SouvenirResponse updateSouvenir(
            Long id,
            SouvenirUpdateRequest request,
            Long userId,
            List<MultipartFile> files
    ) {
        requireUserId(userId);
        Souvenir souvenir = souvenirRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOUVENIR_NOT_FOUND));

        if (!souvenir.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        ExchangeCalculatedPrice price = exchangeRateService.calculatePrice(
                request.countryCode(),
                request.localPrice(),
                request.krwPrice()
        );

        souvenir.update(
                request.name(),
                price.localPrice(),
                request.currencySymbol(),
                price.krwPrice(),
                request.description(),
                request.address(),
                request.locationDetail(),
                request.latitude(),
                request.longitude(),
                request.category(),
                request.purpose(),
                request.countryCode()
        );

        fileService.deleteFilesByEntity("Souvenir", id);
        List<FileResponse> uploadedFiles =
                uploadFiles(souvenir.getId(), userId, files);

        return SouvenirResponse.of(souvenir, uploadedFiles, true);
    }

    @Transactional
    public void deleteSouvenir(Long id, Long userId) {
        requireUserId(userId);
        Souvenir souvenir = souvenirRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOUVENIR_NOT_FOUND));

        if (!souvenir.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        fileService.deleteFilesByEntity("Souvenir", id);
        souvenir.delete();
    }

    private List<FileResponse> uploadFiles(Long souvenirId, Long userId, List<MultipartFile> files) {
        String uuid = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND))
                .getUserId();

        return Optional.ofNullable(files)
                .orElse(List.of())
                .stream()
                .map(file -> fileService.uploadFile(
                        uuid,
                        "Souvenir",
                        souvenirId,
                        file,
                        null
                ))
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
