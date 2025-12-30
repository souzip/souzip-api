package com.souzip.api.domain.country.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.entity.Region;
import com.souzip.api.domain.currency.entity.Currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryExternalDto(
    Name name,
    List<String> capital,
    String region,
    Flags flags,
    String cca2,
    CapitalInfo capitalInfo,
    Map<String, Translation> translations,
    Map<String, CurrencyInfo> currencies
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Name(String common, String official) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Flags(String png, String svg) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Translation(String common, String official) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrencyInfo(String name, String symbol) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CapitalInfo(@JsonProperty("latlng") List<Double> latlng) {}

    public String getKoreanName() {
        return Optional.ofNullable(translations)
            .map(trans -> trans.get("kor"))
            .map(Translation::common)
            .orElseGet(this::getEnglishName);
    }

    public String getEnglishName() {
        return name.common();
    }

    public String getPrimaryCurrencyCode() {
        return Optional.ofNullable(currencies)
            .filter(this::hasCurrencies)
            .map(curr -> curr.keySet().iterator().next())
            .orElse(null);
    }

    public String getPrimaryCurrencySymbol() {
        return Optional.ofNullable(currencies)
            .filter(this::hasCurrencies)
            .map(curr -> curr.values().iterator().next())
            .map(CurrencyInfo::symbol)
            .orElse(null);
    }

    public Country toEntity(Region region, Currency currency) {
        return Country.of(
            getEnglishName(),
            getKoreanName(),
            cca2,
            getFirstCapital(),
            region,
            flags.png(),
            getLatitude(),
            getLongitude(),
            currency
        );
    }

    private boolean hasCurrencies(Map<String, CurrencyInfo> currencies) {
        return currencies != null && !currencies.isEmpty();
    }

    private String getFirstCapital() {
        return getFirstOrNull(capital);
    }

    private BigDecimal getLatitude() {
        return getCapitalCoordinateAt(0);
    }

    private BigDecimal getLongitude() {
        return getCapitalCoordinateAt(1);
    }

    private String getFirstOrNull(List<String> list) {
        return Optional.ofNullable(list)
            .filter(this::hasElements)
            .map(List::getFirst)
            .orElse(null);
    }

    private boolean hasElements(List<?> list) {
        return list != null && !list.isEmpty();
    }

    private BigDecimal getCapitalCoordinateAt(int index) {
        return Optional.ofNullable(capitalInfo)
            .map(CapitalInfo::latlng)
            .filter(coords -> hasIndexInList(coords, index))
            .map(coords -> coords.get(index))
            .map(BigDecimal::valueOf)
            .orElse(null);
    }

    private boolean hasIndexInList(List<?> list, int index) {
        return list != null && list.size() > index;
    }
}
