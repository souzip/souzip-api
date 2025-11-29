package com.souzip.api.domain.country.service;

import com.souzip.api.domain.country.dto.CountryExternalDto;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.repository.CountryRepository;
import com.souzip.api.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CountryService countryService;


    private Country createCountry(String name, String code) {
        return Country.of(
            name,
            code,
            "Seoul",
            "Asia",
            "https://flagcdn.com/w320/kr.png",
            new BigDecimal("37.0"),
            new BigDecimal("127.0")
        );
    }

    private CountryExternalDto[] createExternalDtos() {
        CountryExternalDto.Name name = new CountryExternalDto.Name(
            "South Korea",
            "Republic of Korea"
        );
        CountryExternalDto.Flags flags = new CountryExternalDto.Flags(
            "https://flagcdn.com/w320/kr.png",
            "https://flagcdn.com/kr.svg"
        );

        CountryExternalDto korea = new CountryExternalDto(
            name,
            List.of("Seoul"),
            "Asia",
            flags,
            "KR",
            List.of(37.0, 127.5)
        );

        return new CountryExternalDto[]{korea};
    }


    @Test
    @DisplayName("전체 국가를 조회할 수 있다.")
    void getAllCountries() {
        // given
        Country korea = createCountry("South Korea", "KR");
        Country japan = createCountry("Japan", "JP");

        given(countryRepository.findAll()).willReturn(List.of(korea, japan));

        // when
        List<Country> countries = countryService.getAllCountries();

        // then
        assertThat(countries).hasSize(2);
        assertThat(countries)
            .extracting(Country::getCode)
            .containsExactly("KR", "JP");
    }

    @Test
    @DisplayName("국가 코드로 조회할 수 있다.")
    void getCountryByCode_success() {
        // given
        Country korea = createCountry("South Korea", "KR");
        given(countryRepository.findByCode("KR")).willReturn(Optional.of(korea));

        // when
        Country found = countryService.getCountryByCode("KR");

        // then
        assertThat(found.getName()).isEqualTo("South Korea");
        assertThat(found.getCode()).isEqualTo("KR");
    }

    @Test
    @DisplayName("존재하지 않는 국가 코드 조회 시 예외가 발생한다.")
    void getCountryByCode_notFound() {
        // given
        given(countryRepository.findByCode("SOU")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> countryService.getCountryByCode("SOU"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("대륙별 국가를 조회할 수 있다.")
    void getCountriesByRegion() {
        // given
        Country korea = createCountry("South Korea", "KR");
        Country japan = createCountry("Japan", "JP");

        given(countryRepository.findByRegion("Asia")).willReturn(List.of(korea, japan));

        // when
        List<Country> countries = countryService.getCountriesByRegion("Asia");

        // then
        assertThat(countries).hasSize(2);
        assertThat(countries)
            .extracting(Country::getCode)
            .containsExactlyInAnyOrder("KR", "JP");
    }

    @Test
    @DisplayName("국가명으로 검색할 수 있다.")
    void searchCountriesByName() {
        // given
        Country korea = createCountry("South Korea", "KR");

        given(countryRepository.findByNameContaining("Korea")).willReturn(List.of(korea));

        // when
        List<Country> countries = countryService.searchCountriesByName("Korea");

        // then
        assertThat(countries).hasSize(1);
        assertThat(countries.getFirst().getName()).isEqualTo("South Korea");
    }

    @Test
    @DisplayName("대륙별 국가 개수를 조회할 수 있다.")
    void getCountryCountByRegion() {
        // given
        given(countryRepository.countByRegion("Asia")).willReturn(50L);

        // when
        long count = countryService.getCountryCountByRegion("Asia");

        // then
        assertThat(count).isEqualTo(50);
    }


    @Test
    @DisplayName("외부 API에서 국가 데이터를 가져와 저장할 수 있다.")
    void fetchAndSaveCountries_success() {
        // given
        ReflectionTestUtils.setField(countryService, "baseUrl", "https://restcountries.com/v3.1");

        CountryExternalDto[] response = createExternalDtos();

        given(countryRepository.findAll()).willReturn(List.of());
        given(restTemplate.getForObject(anyString(), eq(CountryExternalDto[].class)))
            .willReturn(response);

        // when
        countryService.fetchAndSaveCountries();

        // then
        then(countryRepository).should().saveAll(argThat((List<Country> countries) ->
            !countries.isEmpty() && countries.getFirst().getCode().equals("KR")
        ));
    }

    @Test
    @DisplayName("중복된 국가는 저장하지 않는다.")
    void fetchAndSaveCountries_skipDuplicates() {
        // given
        ReflectionTestUtils.setField(countryService, "baseUrl", "https://restcountries.com/v3.1");

        Country existingKorea = createCountry("South Korea", "KR");
        CountryExternalDto[] response = createExternalDtos();

        given(countryRepository.findAll()).willReturn(List.of(existingKorea));
        given(restTemplate.getForObject(anyString(), eq(CountryExternalDto[].class)))
            .willReturn(response);

        // when
        countryService.fetchAndSaveCountries();

        // then
        then(countryRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("외부 API가 null을 반환하면 저장하지 않는다.")
    void fetchAndSaveCountries_nullResponse() {
        // given
        ReflectionTestUtils.setField(countryService, "baseUrl", "https://restcountries.com/v3.1");

        given(restTemplate.getForObject(anyString(), eq(CountryExternalDto[].class)))
            .willReturn(null);

        // when
        countryService.fetchAndSaveCountries();

        // then
        then(countryRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("외부 API가 빈 배열을 반환하면 저장하지 않는다.")
    void fetchAndSaveCountries_emptyResponse() {
        // given
        ReflectionTestUtils.setField(countryService, "baseUrl", "https://restcountries.com/v3.1");

        given(restTemplate.getForObject(anyString(), eq(CountryExternalDto[].class)))
            .willReturn(new CountryExternalDto[0]);

        // when
        countryService.fetchAndSaveCountries();

        // then
        then(countryRepository).should(never()).saveAll(any());
    }
}
