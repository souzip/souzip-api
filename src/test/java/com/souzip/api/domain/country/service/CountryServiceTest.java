package com.souzip.api.domain.country.service;

import com.souzip.api.domain.country.dto.CountryExternalDto;
import com.souzip.api.domain.country.dto.CountryExternalDto.Flags;
import com.souzip.api.domain.country.dto.CountryExternalDto.Name;
import com.souzip.api.domain.country.dto.CountryExternalDto.Translation;
import com.souzip.api.domain.country.dto.CountryExternalDto.CurrencyInfo;
import com.souzip.api.domain.country.dto.CountryResponseDto;
import com.souzip.api.domain.country.dto.CountryListResponse;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.entity.Region;
import com.souzip.api.domain.country.repository.CountryRepository;
import com.souzip.api.domain.currency.entity.Currency;
import com.souzip.api.domain.currency.repository.CurrencyRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private CountryExternalApiClient externalApiClient;

    @InjectMocks
    private CountryService countryService;

    private Country createCountry(String nameEn, String nameKr, String code, Currency currency) {
        return Country.of(
            nameEn,
            nameKr,
            code,
            "Seoul",
            Region.ASIA,
            "https://flagcdn.com/w320/kr.png",
            new BigDecimal("37.0"),
            new BigDecimal("127.0"),
            currency
        );
    }

    private Currency createCurrency(String code, String symbol) {
        return Currency.of(code, symbol);
    }

    private CountryExternalDto createCountryExternalDto(
        String nameEn,
        String nameKr,
        String code,
        String region,
        String currencyCode,
        String currencySymbol
    ) {
        Name name = new Name(nameEn, "Republic of " + nameEn);
        Flags flags = new Flags("https://flagcdn.com/w320/kr.png", "https://flagcdn.com/kr.svg");
        Translation translation = new Translation(nameKr, nameKr);
        CurrencyInfo currencyInfo = new CurrencyInfo(currencyCode, currencySymbol);

        return new CountryExternalDto(
            name,
            List.of("Seoul"),
            region,
            flags,
            code,
            List.of(37.0, 127.5),
            Map.of("kor", translation),
            Map.of(currencyCode, currencyInfo)
        );
    }

    // ===== 조회 테스트 =====

    @Test
    @DisplayName("전체 국가를 조회할 수 있다.")
    void getAllCountries() {
        // given
        Currency krw = createCurrency("KRW", "₩");
        Currency jpy = createCurrency("JPY", "¥");
        Country korea = createCountry("South Korea", "대한민국", "KR", krw);
        Country japan = createCountry("Japan", "일본", "JP", jpy);

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
    @DisplayName("국가 코드로 국가를 조회할 수 있다.")
    void getCountryByCode_success() {
        // given
        Currency krw = createCurrency("KRW", "₩");
        Country korea = createCountry("South Korea", "대한민국", "KR", krw);

        given(countryRepository.findByCode("KR")).willReturn(Optional.of(korea));

        // when
        CountryResponseDto found = countryService.getCountryByCode("KR");

        // then
        assertThat(found.nameEn()).isEqualTo("South Korea");
        assertThat(found.nameKr()).isEqualTo("대한민국");
        assertThat(found.code()).isEqualTo("KR");
    }

    @Test
    @DisplayName("존재하지 않는 국가 코드 조회 시 예외가 발생한다.")
    void getCountryByCode_throwsException_whenCountryNotFound() {
        // given
        given(countryRepository.findByCode("INVALID")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> countryService.getCountryByCode("INVALID"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.COUNTRY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("지역별로 국가를 조회할 수 있다.")
    void getCountriesByRegion() {
        // given
        Currency krw = createCurrency("KRW", "₩");
        Currency jpy = createCurrency("JPY", "¥");
        Country korea = createCountry("South Korea", "대한민국", "KR", krw);
        Country japan = createCountry("Japan", "일본", "JP", jpy);

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
        Currency krw = createCurrency("KRW", "₩");
        Country korea = createCountry("South Korea", "대한민국", "KR", krw);

        given(countryRepository.findByNameContaining("Korea")).willReturn(List.of(korea));

        // when
        CountryListResponse response = countryService.searchCountriesByName("Korea");

        // then
        assertThat(response.countries()).hasSize(1);
        assertThat(response.countries().getFirst().nameEn()).isEqualTo("South Korea");
    }

    @Test
    @DisplayName("지역별 국가 수를 조회할 수 있다.")
    void getCountryCountByRegion() {
        // given
        given(countryRepository.countByRegion(Region.ASIA)).willReturn(48L);

        // when
        long count = countryService.getCountryCountByRegion("Asia");

        // then
        assertThat(count).isEqualTo(48L);
    }

    @Test
    @DisplayName("외부 API에서 국가 데이터를 가져와 저장할 수 있다.")
    void fetchAndSaveCountries_savesNewCountries() {
        // given
        CountryExternalDto dto = createCountryExternalDto(
            "South Korea", "대한민국", "KR", "Asia", "KRW", "₩"
        );
        Currency krw = createCurrency("KRW", "₩");

        given(countryRepository.findAll()).willReturn(List.of());
        given(externalApiClient.fetchCountries()).willReturn(List.of(dto));
        given(currencyRepository.findByCode("KRW")).willReturn(Optional.of(krw));

        // when
        countryService.fetchAndSaveCountries();

        // then
        then(countryRepository).should().saveAll(
            argThat((List<Country> countries) ->
                countries.size() == 1 && countries.getFirst().getCode().equals("KR")
            )
        );
    }

    @Test
    @DisplayName("통화 정보가 없으면 새로운 통화를 생성하여 저장한다.")
    void fetchAndSaveCountries_createsNewCurrency_whenCurrencyNotExists() {
        // given
        CountryExternalDto dto = createCountryExternalDto(
            "South Korea", "대한민국", "KR", "Asia", "KRW", "₩"
        );
        Currency krw = createCurrency("KRW", "₩");

        given(countryRepository.findAll()).willReturn(List.of());
        given(externalApiClient.fetchCountries()).willReturn(List.of(dto));
        given(currencyRepository.findByCode("KRW")).willReturn(Optional.empty());
        given(currencyRepository.save(any(Currency.class))).willReturn(krw);

        // when
        countryService.fetchAndSaveCountries();

        // then
        then(currencyRepository).should().save(any(Currency.class));
        then(countryRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("중복된 국가는 저장하지 않는다.")
    void fetchAndSaveCountries_skipsExistingCountries() {
        // given
        Currency krw = createCurrency("KRW", "₩");
        Country existingKorea = createCountry("South Korea", "대한민국", "KR", krw);
        CountryExternalDto dto = createCountryExternalDto(
            "South Korea", "대한민국", "KR", "Asia", "KRW", "₩"
        );

        given(countryRepository.findAll()).willReturn(List.of(existingKorea));
        given(externalApiClient.fetchCountries()).willReturn(List.of(dto));

        // when
        countryService.fetchAndSaveCountries();

        // then
        then(countryRepository).should(never()).saveAll(anyList());
    }

    @Test
    @DisplayName("외부 API가 빈 리스트를 반환하면 저장하지 않는다.")
    void fetchAndSaveCountries_doesNotSave_whenExternalApiReturnsEmptyList() {
        // given
        given(externalApiClient.fetchCountries()).willReturn(List.of());

        // when
        countryService.fetchAndSaveCountries();

        // then
        then(countryRepository).should(never()).saveAll(anyList());
    }

    @Test
    @DisplayName("잘못된 지역 코드가 포함된 DTO는 예외를 발생시킨다.")
    void fetchAndSaveCountries_throwsException_whenRegionIsInvalid() {
        // given
        CountryExternalDto invalidDto = createCountryExternalDto(
            "TestLand", "테스트랜드", "TL", "InvalidRegion", "TEST", "$"
        );

        given(countryRepository.findAll()).willReturn(List.of());
        given(externalApiClient.fetchCountries()).willReturn(List.of(invalidDto));

        // when & then
        assertThatThrownBy(() -> countryService.fetchAndSaveCountries())
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.COUNTRY_REGION_INVALID.getMessage());
    }

    @Test
    @DisplayName("지역 코드는 대소문자를 구분하지 않는다.")
    void fetchAndSaveCountries_handlesCaseInsensitiveRegion() {
        // given
        CountryExternalDto dto = createCountryExternalDto(
            "South Korea", "대한민국", "KR", "asia", "KRW", "₩"
        );
        Currency krw = createCurrency("KRW", "₩");

        given(countryRepository.findAll()).willReturn(List.of());
        given(externalApiClient.fetchCountries()).willReturn(List.of(dto));
        given(currencyRepository.findByCode("KRW")).willReturn(Optional.of(krw));

        // when
        countryService.fetchAndSaveCountries();

        // then
        then(countryRepository).should().saveAll(
            argThat((List<Country> countries) ->
                countries.getFirst().getRegion() == Region.ASIA
            )
        );
    }
}
