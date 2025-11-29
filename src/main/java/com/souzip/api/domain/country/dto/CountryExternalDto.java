package com.souzip.api.domain.country.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.entity.Region;

import java.math.BigDecimal;
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

    public Optional<Region> parseRegion() {
        return Region.fromCode(region);
    }

    public Country toEntity(Region region) {
        return Country.of(
            name.common(),
            cca2,
            getFirstOrNull(capital),
            region,
            flags.png(),
            getCoordinateAtAsBigDecimal(0),
            getCoordinateAtAsBigDecimal(1)
        );
    }

    private String getFirstOrNull(List<String> list) {
        return Optional.ofNullable(list)
            .filter(items -> !items.isEmpty())
            .map(List::getFirst)
            .orElse(null);
    }

    private BigDecimal getCoordinateAtAsBigDecimal(int index) {
        return Optional.ofNullable(latlng)
            .filter(coords -> coords.size() > index)
            .map(coords -> coords.get(index))
            .map(BigDecimal::valueOf)
            .orElse(null);
    }
}
