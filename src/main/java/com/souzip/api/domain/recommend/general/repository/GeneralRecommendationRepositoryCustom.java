package com.souzip.api.domain.recommend.general.repository;

import com.souzip.api.domain.recommend.general.dto.GeneralRecommendationStatsDto;
import com.souzip.api.domain.souvenir.entity.Souvenir;

import java.util.List;

public interface GeneralRecommendationRepositoryCustom {
    List<Souvenir> findTop10ByCountry(String countryCode);
    List<Souvenir> findTop10ByCategoryRecent(String categoryName);
    List<GeneralRecommendationStatsDto> findTop3CountriesBySouvenirCount();
    List<GeneralRecommendationStatsDto> findTop10CountriesBySouvenirCount();
}
