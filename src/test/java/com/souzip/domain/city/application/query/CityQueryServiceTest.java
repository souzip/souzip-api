package com.souzip.domain.city.application.query;

import com.souzip.domain.city.entity.City;
import com.souzip.domain.city.repository.CityRepository;
import com.souzip.domain.country.entity.Country;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CityQueryServiceTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityQueryService cityQueryService;

    @DisplayName("나라 ID로 도시 목록 조회 성공")
    @Test
    void getCities_success() {
        // given
        Long countryId = 1L;

        Country country = mock(Country.class);

        City seoul = City.create("Seoul", "서울",
            BigDecimal.valueOf(37.56), BigDecimal.valueOf(126.97), country);
        City busan = City.create("Busan", "부산",
            BigDecimal.valueOf(35.18), BigDecimal.valueOf(129.08), country);
        seoul.updatePriority(1);
        busan.updatePriority(2);

        given(cityRepository.findByCountryId(countryId)).willReturn(List.of(seoul, busan));

        // when
        List<CityQueryService.CityQueryResult> results = cityQueryService.getCities(countryId);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).nameKr()).isEqualTo("서울");
        assertThat(results.get(0).priority()).isEqualTo(1);
        assertThat(results.get(1).nameKr()).isEqualTo("부산");
        assertThat(results.get(1).priority()).isEqualTo(2);

        verify(cityRepository).findByCountryId(countryId);
    }

    @DisplayName("도시 목록이 없으면 빈 목록 반환")
    @Test
    void getCities_empty() {
        // given
        Long countryId = 999L;
        given(cityRepository.findByCountryId(countryId)).willReturn(List.of());

        // when
        List<CityQueryService.CityQueryResult> results = cityQueryService.getCities(countryId);

        // then
        assertThat(results).isEmpty();
        verify(cityRepository).findByCountryId(countryId);
    }
}
