package com.souzip.api.domain.recommend.general.service;

import com.souzip.api.application.file.FileQueryService;
import com.souzip.api.application.file.dto.FileResponse;
import com.souzip.api.application.file.required.FileStorage;
import com.souzip.api.domain.file.File;
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
    private final FileQueryService fileQueryService;
    private final FileStorage fileStorage;

    public List<GeneralRecommendationDto> getTop10ByCategory(String categoryName) {
        List<Souvenir> souvenirs = generalRecommendationRepository
                .findTop10ByCategoryRecent(categoryName);

        List<Long> souvenirIds = souvenirs.stream()
                .map(Souvenir::getId)
                .toList();

        Map<Long, FileResponse> thumbnailMap = getThumbnails(souvenirIds);

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

    public List<GeneralRecommendationStatsDto> getTop10CountriesBySouvenirCount() {
        return generalRecommendationRepository.findTop10CountriesBySouvenirCount();
    }

    public List<CountryRecommendationDto> getTopCountriesWithTop10Souvenirs() {
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

    public List<GeneralRecommendationDto> getTop10ByCountry(String countryCode) {
        List<Souvenir> souvenirs = generalRecommendationRepository
                .findTop10ByCountry(countryCode);

        List<Long> souvenirIds = souvenirs.stream()
                .map(Souvenir::getId)
                .toList();

        Map<Long, FileResponse> thumbnailMap = getThumbnails(souvenirIds);

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

    public List<GeneralRecommendationStatsDto> getTop3CountriesBySouvenirCount() {
        return generalRecommendationRepository.findTop3CountriesBySouvenirCount();
    }

    private Map<Long, FileResponse> getThumbnails(List<Long> souvenirIds) {
        Map<Long, File> fileMap = fileQueryService.findThumbnailsByEntityIds("Souvenir", souvenirIds);

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
