package com.souzip.domain.recommend.general.controller;

import com.souzip.domain.recommend.general.dto.CountryRecommendationDto;
import com.souzip.domain.recommend.general.dto.GeneralRecommendationDto;
import com.souzip.domain.recommend.general.dto.GeneralRecommendationStatsDto;
import com.souzip.domain.recommend.general.service.GeneralRecommendationService;
import com.souzip.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GeneralRecommendationController {

    private final GeneralRecommendationService generalRecommendationService;

    @GetMapping("/discovery/general/country/{countryCode}")
    public SuccessResponse<List<GeneralRecommendationDto>> getCountryTop10(
            @PathVariable String countryCode,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return SuccessResponse.of(generalRecommendationService.getTop10ByCountry(countryCode, authorizationHeader));
    }

    @GetMapping("/discovery/general/category/{categoryName}")
    public SuccessResponse<List<GeneralRecommendationDto>> getCategoryTop10(
            @PathVariable String categoryName,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return SuccessResponse.of(generalRecommendationService.getTop10ByCategory(categoryName, authorizationHeader));
    }

    @GetMapping("/discovery/general/stats")
    public SuccessResponse<List<GeneralRecommendationStatsDto>> getTopCountriesAllTimeTop3() {
        return SuccessResponse.of(generalRecommendationService.getTop3CountriesBySouvenirCount());
    }

    @GetMapping("/countries/souvenirs")
    public SuccessResponse<List<CountryRecommendationDto>> getTopCountriesWithTop10Souvenirs(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return SuccessResponse.of(generalRecommendationService.getTopCountriesWithTop10Souvenirs(authorizationHeader));
    }

    @GetMapping("/discovery/general/countries/top10")
    public SuccessResponse<List<GeneralRecommendationStatsDto>> getTopCountriesAllTimeTop10() {
        return SuccessResponse.of(generalRecommendationService.getTop10CountriesBySouvenirCount());
    }
}
