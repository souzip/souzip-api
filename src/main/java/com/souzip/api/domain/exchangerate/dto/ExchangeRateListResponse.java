package com.souzip.api.domain.exchangerate.dto;

import java.util.List;

public record ExchangeRateListResponse(
        List<ExchangeRateResponseDto> exchangeRates
) {
    public static ExchangeRateListResponse from(List<ExchangeRateResponseDto> exchangeRates) {
        return new ExchangeRateListResponse(exchangeRates);
    }
}
