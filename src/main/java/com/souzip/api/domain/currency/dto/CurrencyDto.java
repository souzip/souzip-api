package com.souzip.api.domain.currency.dto;

import com.souzip.api.domain.currency.entity.Currency;

public record CurrencyDto(
    String code,
    String symbol
) {
    public static CurrencyDto from(Currency currency) {
        return new CurrencyDto(
            currency.getCode(),
            currency.getSymbol()
        );
    }
}
