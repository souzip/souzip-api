package com.souzip.api.domain.recommend.general.controller;

import com.souzip.api.domain.recommend.general.dto.CountryRecommendationDto;
import com.souzip.api.domain.recommend.general.dto.GeneralRecommendationDto;
import com.souzip.api.domain.recommend.general.dto.GeneralRecommendationStatsDto;
import com.souzip.api.domain.recommend.general.service.GeneralRecommendationService;
import com.souzip.api.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/discovery/general")
public class GeneralRecommendationController {

    private final GeneralRecommendationService generalRecommendationService;

    @GetMapping("/category/{categoryName}")
    public SuccessResponse<List<GeneralRecommendationDto>> getCategoryTop10(@PathVariable String categoryName) {
        return SuccessResponse.of(generalRecommendationService.getTop10ByCategory(categoryName));
    }

    @GetMapping("/country")
    public SuccessResponse<List<CountryRecommendationDto>> getTopCountriesWithTop10Souvenirs() {
        return SuccessResponse.of(generalRecommendationService.getTopCountriesWithTop10Souvenirs());
    }

    @GetMapping("/stats")
    public SuccessResponse<List<GeneralRecommendationStatsDto>> getTopCountriesAllTimeTop3() {
        return SuccessResponse.of(generalRecommendationService.getTop3CountriesBySouvenirCount());
    }
}
