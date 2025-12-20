package com.souzip.api.domain.country.controller;

import com.souzip.api.domain.country.dto.CountryListResponse;
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
}
