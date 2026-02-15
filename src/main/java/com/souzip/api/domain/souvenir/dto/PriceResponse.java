package com.souzip.api.domain.souvenir.dto;

public record PriceResponse(
    PriceDetail original,
    PriceDetail converted
) {

    public static PriceResponse of(
        Integer originalAmount,
        String originalSymbol,
        Integer convertedAmount,
        String convertedSymbol
    ) {
        return new PriceResponse(
            new PriceDetail(originalAmount, originalSymbol),
            new PriceDetail(convertedAmount, convertedSymbol)
        );
    }

    public record PriceDetail(
        Integer amount,
        String symbol
    ) {}
}
