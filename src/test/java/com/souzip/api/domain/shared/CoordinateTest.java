package com.souzip.api.domain.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class CoordinateTest {

    @Test
    void create() {
        Coordinate.of(BigDecimal.valueOf(37.4979), BigDecimal.valueOf(127.0276));
        Coordinate.of(BigDecimal.valueOf(-90), BigDecimal.valueOf(-180));
        Coordinate.of(BigDecimal.valueOf(90), BigDecimal.valueOf(180));
        Coordinate.of(BigDecimal.valueOf(0), BigDecimal.valueOf(0));
    }

    @Test
    void createFail() {
        // null
        assertThatThrownBy(() -> Coordinate.of(null, BigDecimal.valueOf(127.0276)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("위도는 필수입니다");

        assertThatThrownBy(() -> Coordinate.of(BigDecimal.valueOf(37.4979), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("경도는 필수입니다");

        // 위도 범위 초과
        assertThatThrownBy(() -> Coordinate.of(BigDecimal.valueOf(-91), BigDecimal.valueOf(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("위도는 -90 ~ 90 사이여야 합니다");

        assertThatThrownBy(() -> Coordinate.of(BigDecimal.valueOf(91), BigDecimal.valueOf(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("위도는 -90 ~ 90 사이여야 합니다");

        // 경도 범위 초과
        assertThatThrownBy(() -> Coordinate.of(BigDecimal.valueOf(0), BigDecimal.valueOf(-181)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("경도는 -180 ~ 180 사이여야 합니다");

        assertThatThrownBy(() -> Coordinate.of(BigDecimal.valueOf(0), BigDecimal.valueOf(181)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("경도는 -180 ~ 180 사이여야 합니다");
    }

    @Test
    void equality() {
        Coordinate coord1 = Coordinate.of(BigDecimal.valueOf(37.4979), BigDecimal.valueOf(127.0276));
        Coordinate coord2 = Coordinate.of(BigDecimal.valueOf(37.4979), BigDecimal.valueOf(127.0276));

        assertThat(coord1).isEqualTo(coord2);
        assertThat(coord1.hashCode()).isEqualTo(coord2.hashCode());
    }
}
