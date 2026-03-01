package com.souzip.domain.country.service;

import com.souzip.domain.country.client.CountryExternalApiClient;
import com.souzip.domain.country.dto.CountryListResponse;
import com.souzip.domain.country.dto.CountryResponseDto;
import com.souzip.domain.country.entity.Country;
import com.souzip.domain.country.entity.Region;
import com.souzip.domain.country.repository.CountryRepository;
import com.souzip.domain.currency.entity.Currency;
import com.souzip.domain.currency.repository.CurrencyRepository;
import com.souzip.global.exception.BusinessException;
import com.souzip.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

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
}
