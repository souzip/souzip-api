package com.souzip.domain.currency.dto;

import com.souzip.domain.currency.entity.Currency;

public record CurrencyResponse(
        String code,
        String symbol
) {
    public static CurrencyResponse from(Currency currency) {
        return new CurrencyResponse(
                currency.getCode(),
                currency.getSymbol()
        );
    }
}
