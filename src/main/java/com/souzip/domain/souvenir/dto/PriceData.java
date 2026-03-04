package com.souzip.domain.souvenir.dto;

import com.souzip.domain.souvenir.vo.PriceInfo;

public record PriceData(
    PriceInfo originalPrice,
    Integer exchangeAmount,
    String currencySymbol,
    PriceInfo convertedPrice
) {}
