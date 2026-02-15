package com.souzip.api.domain.souvenir.dto;

import com.souzip.api.domain.souvenir.vo.PriceInfo;

public record PriceData(
    PriceInfo originalPrice,
    Integer exchangeAmount,
    String currencySymbol,
    PriceInfo convertedPrice
) {}
