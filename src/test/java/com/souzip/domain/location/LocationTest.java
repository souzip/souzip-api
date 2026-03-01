package com.souzip.domain.location;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class LocationTest {

    @Test
    void create() {
        LocationCreateRequest request = new LocationCreateRequest(
                "강남역",
                "서울특별시 강남구 역삼동 825",
                BigDecimal.valueOf(37.4979),
                BigDecimal.valueOf(127.0276)
        );

        Location location = Location.create(request);

        assertThat(location.getName()).isEqualTo("강남역");
        assertThat(location.getAddress()).isEqualTo("서울특별시 강남구 역삼동 825");
        assertThat(location.getCoordinate()).isNotNull();
        assertThat(location.getCoordinate().getLatitude()).isEqualTo(BigDecimal.valueOf(37.4979));
        assertThat(location.getCoordinate().getLongitude()).isEqualTo(BigDecimal.valueOf(127.0276));
    }

    @Test
    void createFail() {
        assertThatThrownBy(() -> Location.create(
                new LocationCreateRequest(null, "주소", BigDecimal.valueOf(37), BigDecimal.valueOf(127))
        )).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("이름은 필수입니다");

        assertThatThrownBy(() -> Location.create(
                new LocationCreateRequest("강남역", null, BigDecimal.valueOf(37), BigDecimal.valueOf(127))
        )).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("주소는 필수입니다");

        assertThatThrownBy(() -> Location.create(
                new LocationCreateRequest("강남역", "주소", null, BigDecimal.valueOf(127))
        )).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("위도는 필수입니다");

        assertThatThrownBy(() -> Location.create(
                new LocationCreateRequest("강남역", "주소", BigDecimal.valueOf(37), null)
        )).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("경도는 필수입니다");

        assertThatThrownBy(() -> Location.create(
                new LocationCreateRequest("강남역", "주소", BigDecimal.valueOf(100), BigDecimal.valueOf(127))
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("위도는 -90 ~ 90 사이여야 합니다");
    }

    @Test
    void createEnglishName() {
        LocationCreateRequest request = new LocationCreateRequest(
                "Shibuya 109",
                "2-29-1 Dogenzaka, Shibuya City, Tokyo, Japan",
                BigDecimal.valueOf(35.6598),
                BigDecimal.valueOf(139.6983)
        );

        Location location = Location.create(request);

        assertThat(location.getName()).isEqualTo("Shibuya 109");
        assertThat(location.getAddress()).contains("Tokyo");
    }
}
