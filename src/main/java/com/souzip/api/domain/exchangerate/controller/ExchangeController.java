package com.souzip.api.domain.exchangerate.controller;

import com.souzip.api.domain.exchangerate.dto.ExchangeRateResponseDto;
import com.souzip.api.domain.exchangerate.service.ExchangeRateService;
import com.souzip.api.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RequestMapping("/api/exchange-rate")
@RestController
public class ExchangeController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping("/{countryCode}")
    public SuccessResponse<ExchangeRateResponseDto> getRateByCountry(@PathVariable String countryCode) {
        return SuccessResponse.of(exchangeRateService.getRateByCountry(countryCode));
    }

    @GetMapping
    public SuccessResponse<List<ExchangeRateResponseDto>> getRatesByCountries(@RequestParam(required = false) Set<String> countries) {
        return SuccessResponse.of(exchangeRateService.getRatesByCountries(countries));
    }
}
