package com.souzip.domain.exchangerate.dto;

import com.souzip.domain.exchangerate.entity.ExchangeRate;

import java.math.BigDecimal;

public record ExchangeRateResponseDto(
        String baseCode,
        String currencyCode,
        BigDecimal rate
) {
    public static ExchangeRateResponseDto from(ExchangeRate exchangeRate) {
        return new ExchangeRateResponseDto(
                exchangeRate.getBaseCode(),
                exchangeRate.getCurrencyCode(),
                exchangeRate.getRate()
        );
    }
}
