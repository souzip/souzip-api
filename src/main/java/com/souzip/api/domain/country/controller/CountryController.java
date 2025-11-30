package com.souzip.api.domain.country.controller;

import com.souzip.api.domain.country.dto.CountryResponseDto;
import com.souzip.api.domain.country.dto.CountryResponseDto.CountryListResponse;
import com.souzip.api.domain.country.service.CountryService;
import com.souzip.api.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/countries")
@RestController
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public SuccessResponse<CountryListResponse> getAllCountries() {
        return SuccessResponse.of(countryService.getAllCountries());
    }

    @GetMapping("/{code}")
    public SuccessResponse<CountryResponseDto> getCountryByCode(@PathVariable String code) {
        return SuccessResponse.of(countryService.getCountryByCode(code));
    }

    @GetMapping("/region/{englishName}")
    public SuccessResponse<CountryListResponse> getCountriesByRegion(@PathVariable String englishName) {
        return SuccessResponse.of(countryService.getCountriesByRegion(englishName));
    }

    @GetMapping("/search")
    public SuccessResponse<CountryListResponse> searchCountries(@RequestParam String name) {
        return SuccessResponse.of(countryService.searchCountriesByName(name));
    }

    @GetMapping("/region/{englishName}/count")
    public SuccessResponse<Long> getCountryCountByRegion(@PathVariable String englishName) {
        return SuccessResponse.of(countryService.getCountryCountByRegion(englishName));
    }
}
