package com.souzip.api.domain.recommend.general.service;

import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.file.service.FileService;
import com.souzip.api.domain.recommend.general.dto.GeneralRecommendationDto;
import com.souzip.api.domain.recommend.general.dto.GeneralRecommendationStatsDto;
import com.souzip.api.domain.recommend.general.repository.GeneralRecommendationRepositoryCustom;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeneralRecommendationService {

    private final GeneralRecommendationRepositoryCustom generalRecommendationRepositoryCustom;
    private final FileService fileService;

    public List<GeneralRecommendationDto> getTop10ByCategory(String categoryName) {
        List<Souvenir> souvenirs = generalRecommendationRepositoryCustom.findTop10ByCategoryRecent(categoryName);

        return souvenirs.stream()
                .map(souvenir -> {
                    String thumbnailUrl = getThumbnailUrl(souvenir.getId());
                    return GeneralRecommendationDto.of(souvenir, thumbnailUrl);
                })
                .collect(Collectors.toList());
    }

    public List<GeneralRecommendationDto> getTop10ByCountry(String countryCode) {
        List<Souvenir> souvenirs = generalRecommendationRepositoryCustom.findTop10ByCountry(countryCode);

        return souvenirs.stream()
                .map(souvenir -> {
                    String thumbnailUrl = getThumbnailUrl(souvenir.getId());
                    return GeneralRecommendationDto.of(souvenir, thumbnailUrl);
                })
                .collect(Collectors.toList());
    }

    private String getThumbnailUrl(Long souvenirId) {
        FileResponse firstFile = fileService.getFirstFile("Souvenir", souvenirId);
        return firstFile.url();
    }

    public List<GeneralRecommendationStatsDto> getTop3CountriesByCurrentMonth() {
        return generalRecommendationRepositoryCustom.findTop3CountriesByCurrentMonth();
    }
}
