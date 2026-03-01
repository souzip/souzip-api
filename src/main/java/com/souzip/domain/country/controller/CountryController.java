package com.souzip.domain.country.controller;

import com.souzip.domain.country.dto.CountryListResponse;
import com.souzip.domain.country.service.CountryService;
import com.souzip.global.common.dto.SuccessResponse;
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
}
