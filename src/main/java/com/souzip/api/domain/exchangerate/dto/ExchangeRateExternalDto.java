package com.souzip.api.domain.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.souzip.api.domain.exchangerate.entity.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExchangeRateExternalDto(
        @JsonProperty("base_code")
        String baseCode,
        @JsonProperty("conversion_rates")
        Map<String, Double> conversionRates
) {

    public List<ExchangeRate> toEntities() {
        if (conversionRates == null || conversionRates.isEmpty()) {
            return List.of();
        }

        return conversionRates.entrySet().stream()
                .map(entry -> {
                    String currencyCode = entry.getKey();
                    Double rateFromBase = entry.getValue();

                    BigDecimal invertedRate = BigDecimal.valueOf(1.0 / rateFromBase);

                    return ExchangeRate.of(
                            baseCode,
                            currencyCode,
                            invertedRate
                    );
                })
                .toList();
    }

    public static ExchangeRateExternalDto ofMultiple(Map<String, Double> conversionRates, String baseCode) {
        return new ExchangeRateExternalDto(baseCode, conversionRates);
    }
}
