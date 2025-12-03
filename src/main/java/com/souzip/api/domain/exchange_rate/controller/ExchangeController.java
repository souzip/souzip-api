package com.souzip.api.domain.exchange_rate.controller;

import com.souzip.api.domain.exchange_rate.dto.ExchangeRateResponseDto;
import com.souzip.api.domain.exchange_rate.service.ExchangeRateService;
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
    public ExchangeRateResponseDto getRateByCountry(@PathVariable String countryCode) {
        return exchangeRateService.getRateByCountry(countryCode);
    }

    @GetMapping
    public List<ExchangeRateResponseDto> getRatesByCountries(@RequestParam(required = false) Set<String> countries) {
        return exchangeRateService.getRatesByCountries(countries);
    }

}
