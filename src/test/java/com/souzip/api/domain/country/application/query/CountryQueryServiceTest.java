package com.souzip.api.domain.country.application.query;

import com.souzip.api.domain.country.application.port.CountryAdminPort.CountryAdminResult;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.repository.CountryRepository;
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
class CountryQueryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryQueryService countryQueryService;

    @DisplayName("나라 목록 전체 조회 성공")
    @Test
    void getCountries_withoutKeyword_success() {
        // given
        Country korea = mock(Country.class);
        given(korea.getId()).willReturn(1L);
        given(korea.getNameKr()).willReturn("대한민국");

        Country japan = mock(Country.class);
        given(japan.getId()).willReturn(2L);
        given(japan.getNameKr()).willReturn("일본");

        given(countryRepository.findAllByOrderByNameKrAsc()).willReturn(List.of(korea, japan));

        // when
        List<CountryAdminResult> results = countryQueryService.getCountries(null);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).nameKr()).isEqualTo("대한민국");
        assertThat(results.get(1).nameKr()).isEqualTo("일본");

        verify(countryRepository).findAllByOrderByNameKrAsc();
    }

    @DisplayName("나라 키워드 검색 성공")
    @Test
    void getCountries_withKeyword_success() {
        // given
        Country korea = mock(Country.class);
        given(korea.getId()).willReturn(1L);
        given(korea.getNameKr()).willReturn("대한민국");

        given(countryRepository.findByKeywordOrderByNameKrAsc("한국"))
                .willReturn(List.of(korea));

        // when
        List<CountryAdminResult> results = countryQueryService.getCountries("한국");

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().nameKr()).isEqualTo("대한민국");

        verify(countryRepository).findByKeywordOrderByNameKrAsc("한국");
    }

    @DisplayName("나라 목록이 없으면 빈 목록 반환")
    @Test
    void getCountries_empty() {
        // given
        given(countryRepository.findAllByOrderByNameKrAsc()).willReturn(List.of());

        // when
        List<CountryAdminResult> results = countryQueryService.getCountries(null);

        // then
        assertThat(results).isEmpty();
        verify(countryRepository).findAllByOrderByNameKrAsc();
    }

    @DisplayName("빈 키워드로 검색하면 전체 조회")
    @Test
    void getCountries_withBlankKeyword_success() {
        // given
        Country korea = mock(Country.class);
        given(korea.getId()).willReturn(1L);
        given(korea.getNameKr()).willReturn("대한민국");

        given(countryRepository.findAllByOrderByNameKrAsc()).willReturn(List.of(korea));

        // when
        List<CountryAdminResult> results = countryQueryService.getCountries("");

        // then
        assertThat(results).hasSize(1);
        verify(countryRepository).findAllByOrderByNameKrAsc();
    }
}
