package com.souzip.domain.shared;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.Assert.state;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coordinate {

    private BigDecimal latitude;
    private BigDecimal longitude;

    private Coordinate(BigDecimal latitude, BigDecimal longitude) {
        requireNonNull(latitude, "위도는 필수입니다.");
        requireNonNull(longitude, "경도는 필수입니다.");

        validateLatitude(latitude);
        validateLongitude(longitude);

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Coordinate of(BigDecimal latitude, BigDecimal longitude) {
        return new Coordinate(latitude, longitude);
    }

    private static void validateLatitude(BigDecimal latitude) {
        state(latitude.compareTo(BigDecimal.valueOf(-90)) >= 0 &&
                        latitude.compareTo(BigDecimal.valueOf(90)) <= 0,
                "위도는 -90 ~ 90 사이여야 합니다.");
    }

    private static void validateLongitude(BigDecimal longitude) {
        state(longitude.compareTo(BigDecimal.valueOf(-180)) >= 0 &&
                        longitude.compareTo(BigDecimal.valueOf(180)) <= 0,
                "경도는 -180 ~ 180 사이여야 합니다.");
    }
}
