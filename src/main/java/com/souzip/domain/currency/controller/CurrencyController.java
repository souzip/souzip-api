package com.souzip.domain.currency.controller;

import com.souzip.auth.adapter.security.annotation.RequireAuth;
import com.souzip.domain.currency.dto.CurrencyResponse;
import com.souzip.domain.currency.service.CurrencyService;
import com.souzip.shared.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/currency")
@RestController
public class CurrencyController {

    private final CurrencyService currencyService;

    @RequireAuth
    @GetMapping("/{countryCode}")
    public SuccessResponse<CurrencyResponse> getCurrencyByCountry(
            @PathVariable String countryCode
    ) {
        return SuccessResponse.of(currencyService.getCurrencyByCountryCode(countryCode));
    }
}
