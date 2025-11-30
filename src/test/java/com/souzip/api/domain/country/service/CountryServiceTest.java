package com.souzip.api.domain.country.service;

import com.souzip.api.domain.country.dto.CountryExternalDto;
import com.souzip.api.domain.country.dto.CountryExternalDto.Flags;
import com.souzip.api.domain.country.dto.CountryExternalDto.Name;
import com.souzip.api.domain.country.dto.CountryResponseDto;
import com.souzip.api.domain.country.dto.CountryListResponse;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.entity.Region;
import com.souzip.api.domain.country.repository.CountryRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private CountryExternalApiClient externalApiClient;

    @InjectMocks
    private CountryService countryService;

    private Country createCountry(String name, String code) {
        return Country.of(
            name,
            code,
            "Seoul",
            Region.ASIA,
            "https://flagcdn.com/w320/kr.png",
            new BigDecimal("37.0"),
            new BigDecimal("127.0")
        );
    }

    private List<CountryExternalDto> createExternalDtoList() {
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

        return List.of(korea);
    }

    @Test
    @DisplayName("전체 국가를 조회할 수 있다.")
    void getAllCountries() {
        // given
        Country korea = createCountry("South Korea", "KR");
        Country japan = createCountry("Japan", "JP");
        given(countryRepository.findAll()).willReturn(List.of(korea, japan));

        // when
        CountryListResponse response = countryService.getAllCountries();

        // then
        assertThat(response.countries()).hasSize(2);
        assertThat(response.countries())
            .extracting(CountryResponseDto::code)
            .containsExactly("KR", "JP");
    }

    @Test
    @DisplayName("국가 코드로 조회할 수 있다.")
    void getCountryByCode_success() {
        // given
        Country korea = createCountry("South Korea", "KR");
        given(countryRepository.findByCode("KR")).willReturn(Optional.of(korea));

        // when
        CountryResponseDto found = countryService.getCountryByCode("KR");

        // then
        assertThat(found.name()).isEqualTo("South Korea");
        assertThat(found.code()).isEqualTo("KR");
    }

    @Test
    @DisplayName("존재하지 않는 국가 코드 조회 시 예외 발생")
    void getCountryByCode_notFound() {
        // given
        given(countryRepository.findByCode("SOU")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> countryService.getCountryByCode("SOU"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.COUNTRY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("지역별 국가를 조회할 수 있다.")
    void getCountriesByRegion() {
        // given
        Country korea = createCountry("South Korea", "KR");
        Country japan = createCountry("Japan", "JP");
        given(countryRepository.findByRegion(Region.ASIA)).willReturn(List.of(korea, japan));

        // when
        CountryListResponse response = countryService.getCountriesByRegion("Asia");

        // then
        assertThat(response.countries()).hasSize(2);
    }

    @Test
    @DisplayName("국가명으로 검색할 수 있다.")
    void searchCountriesByName() {
        // given
        Country korea = createCountry("South Korea", "KR");
        given(countryRepository.findByNameContaining("Korea")).willReturn(List.of(korea));

        // when
        CountryListResponse response = countryService.searchCountriesByName("Korea");

        // then
        assertThat(response.countries()).hasSize(1);
        assertThat(response.countries().getFirst().name()).isEqualTo("South Korea");
    }

    @Test
    @DisplayName("외부 API에서 국가 데이터를 가져와 저장할 수 있다.")
    void fetchAndSaveCountries_success() {
        // given
        List<CountryExternalDto> externalDtos = createExternalDtoList();

        given(countryRepository.findAll()).willReturn(List.of());
        given(externalApiClient.fetchCountries()).willReturn(externalDtos);

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
        Country existingKorea = createCountry("South Korea", "KR");
        List<CountryExternalDto> externalDtos = createExternalDtoList();

        given(countryRepository.findAll()).willReturn(List.of(existingKorea));
        given(externalApiClient.fetchCountries()).willReturn(externalDtos);

        // when
        countryService.fetchAndSaveCountries();

        // then
        then(countryRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("외부 API가 빈 리스트를 반환하면 저장하지 않는다.")
    void fetchAndSaveCountries_emptyResponse() {
        // given
        given(externalApiClient.fetchCountries()).willReturn(List.of());

        // when
        countryService.fetchAndSaveCountries();

        // then
        then(countryRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("잘못된 region 코드 DTO는 예외 발생")
    void fetchAndSaveCountries_invalidRegion() {
        // given
        CountryExternalDto.Name name = new CountryExternalDto.Name("TestLand", "TestLand Official");
        CountryExternalDto.Flags flags = new CountryExternalDto.Flags("png", "svg");
        CountryExternalDto invalidDto = new CountryExternalDto(
            name,
            List.of("Capital"),
            "InvalidRegion",
            flags,
            "TL",
            List.of(10.0, 20.0)
        );

        given(countryRepository.findAll()).willReturn(List.of());
        given(externalApiClient.fetchCountries()).willReturn(List.of(invalidDto));

        // when & then
        assertThatThrownBy(() -> countryService.fetchAndSaveCountries())
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.COUNTRY_REGION_INVALID.getMessage());
    }

    @Test
    @DisplayName("region 코드 대소문자 처리")
    void fetchAndSaveCountries_regionCaseInsensitive() {
        // given
        Name name = new Name("South Korea", "Republic of Korea");
        Flags flags = new Flags("png", "svg");
        CountryExternalDto dto = new CountryExternalDto(
            name,
            List.of("Seoul"),
            "asia",  // 소문자
            flags,
            "KR",
            List.of(37.0, 127.0)
        );

        given(countryRepository.findAll()).willReturn(List.of());
        given(externalApiClient.fetchCountries()).willReturn(List.of(dto));

        // when
        countryService.fetchAndSaveCountries();

        // then
        then(countryRepository).should().saveAll(
            argThat((List<Country> countries) -> countries.getFirst().getRegion() == Region.ASIA)
        );
    }
}
