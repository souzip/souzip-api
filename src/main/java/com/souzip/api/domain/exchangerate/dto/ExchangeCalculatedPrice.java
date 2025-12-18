package com.souzip.api.domain.exchangerate.dto;

public record ExchangeCalculatedPrice(
        Integer localPrice,
        Integer krwPrice,
        String currencySymbol
) {}
