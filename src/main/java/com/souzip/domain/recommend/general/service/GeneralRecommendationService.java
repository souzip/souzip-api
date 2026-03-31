package com.souzip.domain.recommend.general.service;

import com.souzip.application.file.FileQueryService;
import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.file.required.FileStorage;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.file.File;
import com.souzip.domain.recommend.general.dto.CountryRecommendationDto;
import com.souzip.domain.recommend.general.dto.GeneralRecommendationDto;
import com.souzip.domain.recommend.general.dto.GeneralRecommendationStatsDto;
import com.souzip.domain.recommend.general.repository.GeneralRecommendationRepositoryCustom;
import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.wishlist.repository.WishlistRepository;
import com.souzip.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GeneralRecommendationService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;
    private final GeneralRecommendationRepositoryCustom generalRecommendationRepository;
    private final FileQueryService fileQueryService;
    private final FileStorage fileStorage;
    private final WishlistRepository wishlistRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public List<GeneralRecommendationDto> getTop10ByCategory(String categoryName, @Nullable String authorizationHeader) {
        List<Souvenir> souvenirs = generalRecommendationRepository.findTop10ByCategoryRecent(categoryName);
        return toDto(souvenirs, authorizationHeader);
    }

    public List<GeneralRecommendationStatsDto> getTop10CountriesBySouvenirCount() {
        return generalRecommendationRepository.findTop10CountriesBySouvenirCount();
    }

    public List<CountryRecommendationDto> getTopCountriesWithTop10Souvenirs(@Nullable String authorizationHeader) {
        List<GeneralRecommendationStatsDto> topCountries =
                generalRecommendationRepository.findTop10CountriesBySouvenirCount();

        if (topCountries.isEmpty()) {
            return List.of();
        }

        Map<String, List<Souvenir>> souvenirsByCountry = new LinkedHashMap<>();
        for (GeneralRecommendationStatsDto country : topCountries) {
            List<Souvenir> souvenirs = generalRecommendationRepository.findTop10ByCountry(country.countryCode());
            souvenirsByCountry.put(country.countryCode(), souvenirs);
        }

        List<Long> allSouvenirIds = souvenirsByCountry.values().stream()
                .flatMap(List::stream)
                .map(Souvenir::getId)
                .distinct()
                .toList();

        Map<Long, FileResponse> thumbnailMap = allSouvenirIds.isEmpty()
                ? Map.of()
                : getThumbnails(allSouvenirIds);

        String userId = extractUserId(authorizationHeader);
        Set<Long> wishlistedIds = userId != null
                ? wishlistRepository.findSouvenirIdsByUserId(Long.valueOf(userId))
                : Collections.emptySet();
        Map<Long, Long> wishlistCountMap = wishlistRepository.countBySouvenirIds(allSouvenirIds);

        List<CountryRecommendationDto> result = new ArrayList<>();
        for (GeneralRecommendationStatsDto stats : topCountries) {
            String countryCode = stats.countryCode();
            List<Souvenir> souvenirs = souvenirsByCountry.getOrDefault(countryCode, List.of());

            List<GeneralRecommendationDto> items = souvenirs.stream()
                    .map(s -> GeneralRecommendationDto.of(
                            s,
                            Optional.ofNullable(thumbnailMap.get(s.getId()))
                                    .map(FileResponse::url)
                                    .orElse(null),
                            wishlistCountMap.getOrDefault(s.getId(), 0L),
                            userId != null ? wishlistedIds.contains(s.getId()) : null
                    ))
                    .toList();

            result.add(new CountryRecommendationDto(
                    stats.countryCode(),
                    stats.countryNameKr(),
                    stats.souvenirCount(),
                    items
            ));
        }

        return result;
    }

    public List<GeneralRecommendationDto> getTop10ByCountry(String countryCode, @Nullable String authorizationHeader) {
        List<Souvenir> souvenirs = generalRecommendationRepository.findTop10ByCountry(countryCode);
        return toDto(souvenirs, authorizationHeader);
    }

    public List<GeneralRecommendationStatsDto> getTop3CountriesBySouvenirCount() {
        return generalRecommendationRepository.findTop3CountriesBySouvenirCount();
    }

    private List<GeneralRecommendationDto> toDto(List<Souvenir> souvenirs, @Nullable String authorizationHeader) {
        List<Long> souvenirIds = souvenirs.stream().map(Souvenir::getId).toList();
        Map<Long, FileResponse> thumbnailMap = souvenirIds.isEmpty() ? Map.of() : getThumbnails(souvenirIds);

        String userId = extractUserId(authorizationHeader);
        Set<Long> wishlistedIds = userId != null
                ? wishlistRepository.findSouvenirIdsByUserId(Long.valueOf(userId))
                : Collections.emptySet();
        Map<Long, Long> wishlistCountMap = wishlistRepository.countBySouvenirIds(souvenirIds);

        return souvenirs.stream()
                .map(s -> GeneralRecommendationDto.of(
                        s,
                        Optional.ofNullable(thumbnailMap.get(s.getId()))
                                .map(FileResponse::url)
                                .orElse(null),
                        wishlistCountMap.getOrDefault(s.getId(), 0L),
                        userId != null ? wishlistedIds.contains(s.getId()) : null
                ))
                .toList();
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

    private Map<Long, FileResponse> getThumbnails(List<Long> souvenirIds) {
        Map<Long, File> fileMap = fileQueryService.findThumbnailsByEntityIds(EntityType.SOUVENIR, souvenirIds);

        return fileMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> FileResponse.of(
                                entry.getValue(),
                                fileStorage.generateUrl(entry.getValue().getStorageKey())
                        )
                ));
    }
}
