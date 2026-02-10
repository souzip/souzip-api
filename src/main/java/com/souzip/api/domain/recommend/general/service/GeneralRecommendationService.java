package com.souzip.api.domain.recommend.general.service;

import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.file.service.FileService;
import com.souzip.api.domain.recommend.general.dto.CountryRecommendationDto;
import com.souzip.api.domain.recommend.general.dto.GeneralRecommendationDto;
import com.souzip.api.domain.recommend.general.dto.GeneralRecommendationStatsDto;
import com.souzip.api.domain.recommend.general.repository.GeneralRecommendationRepositoryCustom;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GeneralRecommendationService {

    private final GeneralRecommendationRepositoryCustom generalRecommendationRepository;
    private final FileService fileService;

    public List<GeneralRecommendationDto> getTop10ByCategory(String categoryName) {
        List<Souvenir> souvenirs = generalRecommendationRepository
            .findTop10ByCategoryRecent(categoryName);

        List<Long> souvenirIds = souvenirs.stream()
            .map(Souvenir::getId)
            .toList();

        Map<Long, FileResponse> thumbnailMap = fileService
            .getThumbnailsByEntityIds("Souvenir", souvenirIds);

        return souvenirs.stream()
            .map(s -> new GeneralRecommendationDto(
                s.getId(),
                s.getName(),
                s.getCategory(),
                s.getCountryCode(),
                Optional.ofNullable(thumbnailMap.get(s.getId()))
                    .map(FileResponse::url)
                    .orElse(null)
            ))
            .toList();
    }

    /**
     * ✅ 신규 요구사항:
     * - countryCode 입력 없이 호출
     * - 기념품 등록 수 기준 상위 국가 최대 10개(누적)
     * - 각 국가별 기념품 10개씩 반환
     */
    public List<CountryRecommendationDto> getTopCountriesWithTop10Souvenirs() {
        // 1) Top(최대10) 국가 선정(누적 count desc)
        List<GeneralRecommendationStatsDto> topCountries =
                generalRecommendationRepository.findTop10CountriesBySouvenirCount();

        if (topCountries.isEmpty()) {
            return List.of();
        }

        // 2) 각 국가별 기념품 10개씩 조회 (최대 10번 호출, 정확성 우선)
        //    ※ 성능 최적화 필요하면 "IN + window function" 방식으로 한 번에 가져오는 쿼리로 바꾸면 됨.
        Map<String, List<Souvenir>> souvenirsByCountry = new LinkedHashMap<>();
        for (GeneralRecommendationStatsDto country : topCountries) {
            List<Souvenir> souvenirs = generalRecommendationRepository.findTop10ByCountry(country.countryCode());
            souvenirsByCountry.put(country.countryCode(), souvenirs);
        }

        // 3) 썸네일은 한 번에 조회
        List<Long> allSouvenirIds = souvenirsByCountry.values().stream()
                .flatMap(List::stream)
                .map(Souvenir::getId)
                .distinct()
                .toList();

        Map<Long, FileResponse> thumbnailMap = allSouvenirIds.isEmpty()
                ? Map.of()
                : fileService.getThumbnailsByEntityIds("Souvenir", allSouvenirIds);

        // 4) 국가별 응답 DTO 구성 (topCountries 순서 유지)
        Map<String, GeneralRecommendationStatsDto> statsMap = topCountries.stream()
                .collect(Collectors.toMap(
                        GeneralRecommendationStatsDto::countryCode,
                        s -> s,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<CountryRecommendationDto> result = new ArrayList<>();
        for (GeneralRecommendationStatsDto stats : topCountries) {
            String countryCode = stats.countryCode();
            List<Souvenir> souvenirs = souvenirsByCountry.getOrDefault(countryCode, List.of());

            List<GeneralRecommendationDto> items = souvenirs.stream()
                    .map(s -> GeneralRecommendationDto.of(
                            s,
                            Optional.ofNullable(thumbnailMap.get(s.getId()))
                                    .map(FileResponse::url)
                                    .orElse(null)
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

    public List<GeneralRecommendationStatsDto> getTop3CountriesByCurrentMonth() {
        return generalRecommendationRepository.findTop3CountriesByCurrentMonth();
    }
}
