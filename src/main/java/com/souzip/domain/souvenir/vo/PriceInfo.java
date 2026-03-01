package com.souzip.domain.souvenir.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PriceInfo {

    private final Integer amount;
    private final String currency;

    public static PriceInfo of(Integer amount, String currency) {
        return new PriceInfo(amount, currency);
    }

    public boolean isKrw() {
        return "KRW".equals(currency);
    }
}
