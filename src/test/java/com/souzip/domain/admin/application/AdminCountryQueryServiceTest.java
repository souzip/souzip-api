package com.souzip.domain.admin.application;

import com.souzip.domain.admin.application.port.CountryQueryPort;
import com.souzip.domain.admin.application.port.CountryQueryPort.CountryQueryResult;
import com.souzip.domain.admin.application.query.AdminCountryQueryService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminCountryQueryServiceTest {

    @Mock
    private CountryQueryPort countryQueryPort;

    @InjectMocks
    private AdminCountryQueryService adminCountryQueryService;

    @DisplayName("나라 목록 전체 조회 성공")
    @Test
    void getCountries_withoutKeyword_success() {
        // given
        List<CountryQueryResult> expected = List.of(
                new CountryQueryResult(1L, "대한민국"),
                new CountryQueryResult(2L, "일본"),
                new CountryQueryResult(3L, "미국")
        );

        given(countryQueryPort.getCountries(null)).willReturn(expected);

        // when
        List<CountryQueryResult> result = adminCountryQueryService.getCountries(null);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).nameKr()).isEqualTo("대한민국");
        assertThat(result.get(1).nameKr()).isEqualTo("일본");
        assertThat(result.get(2).nameKr()).isEqualTo("미국");

        verify(countryQueryPort).getCountries(null);
    }

    @DisplayName("나라 키워드 검색 성공")
    @Test
    void getCountries_withKeyword_success() {
        // given
        List<CountryQueryResult> expected = List.of(
                new CountryQueryResult(1L, "대한민국")
        );

        given(countryQueryPort.getCountries("한국")).willReturn(expected);

        // when
        List<CountryQueryResult> result = adminCountryQueryService.getCountries("한국");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().nameKr()).isEqualTo("대한민국");

        verify(countryQueryPort).getCountries("한국");
    }

    @DisplayName("나라 목록이 비어있는 경우 빈 리스트 반환")
    @Test
    void getCountries_empty() {
        // given
        given(countryQueryPort.getCountries(null)).willReturn(List.of());

        // when
        List<CountryQueryResult> result = adminCountryQueryService.getCountries(null);

        // then
        assertThat(result).isEmpty();

        verify(countryQueryPort).getCountries(null);
    }
}
