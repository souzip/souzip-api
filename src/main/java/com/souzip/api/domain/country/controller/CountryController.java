package com.souzip.api.domain.country.controller;

import com.souzip.api.domain.country.dto.CountryResponseDto;
import com.souzip.api.domain.country.service.CountryService;
import com.souzip.api.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public SuccessResponse<List<CountryResponseDto>> getAllCountries() {
        List<CountryResponseDto> countries = countryService.getAllCountries();
        return SuccessResponse.of(countries);
    }

    @GetMapping("/{code}")
    public SuccessResponse<CountryResponseDto> getCountryByCode(@PathVariable String code) {
        CountryResponseDto country = countryService.getCountryByCode(code);
        return SuccessResponse.of(country);
    }

    @GetMapping("/region/{englishName}")
    public SuccessResponse<List<CountryResponseDto>> getCountriesByRegion(@PathVariable String englishName) {
        List<CountryResponseDto> countries = countryService.getCountriesByRegion(englishName);
        return SuccessResponse.of(countries);
    }

    @GetMapping("/search")
    public SuccessResponse<List<CountryResponseDto>> searchCountries(@RequestParam String name) {
        List<CountryResponseDto> countries = countryService.searchCountriesByName(name);
        return SuccessResponse.of(countries);
    }

    @GetMapping("/region/{englishName}/count")
    public SuccessResponse<Long> getCountryCountByRegion(@PathVariable String englishName) {
        Long count = countryService.getCountryCountByRegion(englishName);
        return SuccessResponse.of(count);
    }
}
