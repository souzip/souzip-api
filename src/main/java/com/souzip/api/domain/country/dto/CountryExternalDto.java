package com.souzip.api.domain.country.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.souzip.api.domain.country.entity.Country;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryExternalDto(
    Name name,
    List<String> capital,
    String region,
    Flags flags,
    String cca2,
    List<Double> latlng
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Name(String common, String official) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Flags(String png, String svg) {}

    public Country toEntity() {
        return Country.of(
            name.common(),
            cca2,
            getFirstOrNull(capital),
            region,
            flags.png(),
            getCoordinateAt(0),
            getCoordinateAt(1)
        );
    }

    private String getFirstOrNull(List<String> list) {
        return Optional.ofNullable(list)
            .filter(items -> !items.isEmpty())
            .map(items -> items.get(0))
            .orElse(null);
    }

    private Double getCoordinateAt(int index) {
        return Optional.ofNullable(latlng)
            .filter(coords -> coords.size() > index)
            .map(coords -> coords.get(index))
            .orElse(null);
    }
}
