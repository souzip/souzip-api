package com.souzip.api.domain.recommend.general.service;

import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.file.service.FileService;
import com.souzip.api.domain.recommend.general.dto.GeneralRecommendationDto;
import com.souzip.api.domain.recommend.general.dto.GeneralRecommendationStatsDto;
import com.souzip.api.domain.recommend.general.repository.GeneralRecommendationRepositoryCustom;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public List<GeneralRecommendationStatsDto> getTop10CountriesBySouvenirCount() {
        return generalRecommendationRepository.findTop10CountriesBySouvenirCount();
    }

    public List<GeneralRecommendationDto> getTop10ByCountry(String countryCode) {
        List<Souvenir> souvenirs = generalRecommendationRepository
                .findTop10ByCountry(countryCode);

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

    public List<GeneralRecommendationStatsDto> getTop3CountriesBySouvenirCount() {
        return generalRecommendationRepository.findTop3CountriesBySouvenirCount();
    }
}
