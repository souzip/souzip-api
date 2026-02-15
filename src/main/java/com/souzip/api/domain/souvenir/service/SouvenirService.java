package com.souzip.api.domain.souvenir.service;

import com.souzip.api.domain.audit.entity.AuditAction;
import com.souzip.api.domain.country.repository.CountryRepository;
import com.souzip.api.domain.currency.entity.Currency;
import com.souzip.api.domain.currency.repository.CurrencyRepository;
import com.souzip.api.domain.exchangerate.dto.ExchangeCalculatedPrice;
import com.souzip.api.domain.exchangerate.service.ExchangeRateService;
import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.file.service.FileService;
import com.souzip.api.domain.file.service.FileStorageService;
import com.souzip.api.domain.souvenir.dto.*;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import com.souzip.api.domain.souvenir.repository.SouvenirRepository;
import com.souzip.api.domain.souvenir.vo.PriceInfo;
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
    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;
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

    @Audit(action = AuditAction.SOUVENIR_DELETED)
    @Transactional
    public void deleteSouvenir(Long id, Long userId) {
        requireUserId(userId);

        Souvenir souvenir = findSouvenirWithOwnershipCheck(id, userId);

        fileService.deleteFilesByEntity(ENTITY_TYPE_SOUVENIR, id);
        souvenir.delete();
    }

    // ==================== v1 API ====================

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

    @Audit(action = AuditAction.SOUVENIR_CREATED)
    @Transactional
    public SouvenirResponse createSouvenirV2(
        SouvenirRequest request,
        Long userId,
        List<MultipartFile> files
    ) {
        requireUserId(userId);
        User user = validateUser(userId);

        PriceData priceData = calculatePriceData(request.price(), request.currency(), request.countryCode());

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

        PriceData priceData = calculatePriceData(request.price(), request.currency(), request.countryCode());

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

    private PriceData calculatePriceData(Integer price, String currency, String countryCode) {
        if (price == null || currency == null) {
            return new PriceData(null, null, null, null);
        }

        PriceInfo originalPrice = PriceInfo.of(price, currency);
        Integer exchangeAmount = exchangeRateService.convertToKrw(price, currency);
        String currencySymbol = getCurrencySymbol(currency);

        String localCurrency = getLocalCurrency(countryCode);
        PriceInfo convertedPrice = calculateConvertedPrice(originalPrice, localCurrency, exchangeAmount);

        return new PriceData(originalPrice, exchangeAmount, currencySymbol, convertedPrice);
    }

    private PriceInfo calculateConvertedPrice(PriceInfo originalPrice, String localCurrency, Integer exchangeAmount) {
        if (isKrwInput(originalPrice)) {
            return convertKrwToLocal(originalPrice.getAmount(), localCurrency);
        }
        return PriceInfo.of(exchangeAmount, "KRW");
    }

    private boolean isKrwInput(PriceInfo priceInfo) {
        return "KRW".equals(priceInfo.getCurrency());
    }

    private PriceInfo convertKrwToLocal(Integer krwAmount, String localCurrency) {
        Integer localAmount = exchangeRateService.convertFromKrw(krwAmount, localCurrency);
        return PriceInfo.of(localAmount, localCurrency);
    }

    private PriceResponse createPriceResponse(Souvenir souvenir) {
        PriceInfo originalPrice = souvenir.getOriginalPrice();
        if (originalPrice == null) {
            return null;
        }

        PriceInfo convertedPrice = souvenir.getConvertedPrice();

        String originalSymbol = getCurrencySymbol(originalPrice.getCurrency());
        String convertedSymbol = getCurrencySymbol(convertedPrice.getCurrency());

        return PriceResponse.of(
            originalPrice.getAmount(),
            originalSymbol,
            convertedPrice.getAmount(),
            convertedSymbol
        );
    }

    private String getLocalCurrency(String countryCode) {
        return countryRepository.findByCodeWithCurrency(countryCode)
            .map(country -> country.getCurrency().getCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_NOT_FOUND));
    }

    private String getCurrencySymbol(String currencyCode) {
        if (currencyCode == null) {
            return null;
        }

        return currencyRepository.findByCode(currencyCode)
            .map(Currency::getSymbol)
            .orElseThrow(() -> new BusinessException(ErrorCode.CURRENCY_NOT_FOUND));
    }

    // ==================== Private Helper Methods ====================

    private PriceResponse createPriceResponse(Souvenir souvenir) {
        PriceInfo originalPrice = souvenir.getOriginalPrice();
        if (originalPrice == null) {
            return null;
        }

        PriceInfo convertedPrice = souvenir.getConvertedPrice();

        String originalSymbol = getCurrencySymbol(originalPrice.getCurrency());
        String convertedSymbol = getCurrencySymbol(convertedPrice.getCurrency());

        return PriceResponse.of(
            originalPrice.getAmount(),
            originalSymbol,
            convertedPrice.getAmount(),
            convertedSymbol
        );
    }

    private String getLocalCurrency(String countryCode) {
        return countryRepository.findByCodeWithCurrency(countryCode)
            .map(country -> country.getCurrency().getCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_NOT_FOUND));
    }

    private String getCurrencySymbol(String currencyCode) {
        if (currencyCode == null) {
            return null;
        }

        return currencyRepository.findByCode(currencyCode)
            .map(Currency::getSymbol)
            .orElseThrow(() -> new BusinessException(ErrorCode.CURRENCY_NOT_FOUND));
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

    record PriceData(
        PriceInfo originalPrice,
        Integer exchangeAmount,
        String currencySymbol,
        PriceInfo convertedPrice
    ) {}
}
