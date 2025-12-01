package com.souzip.api.domain.exchange_rate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.souzip.api.domain.exchange_rate.entity.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExchangeRateExternalDto(
        @JsonProperty("base_code")
        String baseCode,
        @JsonProperty("conversion_rates")
        Map<String, Double> conversionRates
) {

    public List<ExchangeRate> toEntities() {
        if (conversionRates == null || conversionRates.isEmpty()) return List.of();

        return conversionRates.entrySet().stream()
                .map(entry -> ExchangeRate.of(
                        entry.getKey(),
                        baseCode,
                        BigDecimal.valueOf(1.0 / entry.getValue())
                ))
                .toList();
    }

    public static ExchangeRateExternalDto ofMultiple(Map<String, Double> conversionRates, String baseCode) {
        return new ExchangeRateExternalDto(baseCode, conversionRates);
    }
}
