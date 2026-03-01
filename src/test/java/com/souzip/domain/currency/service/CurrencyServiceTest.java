package com.souzip.domain.currency.service;

import com.souzip.domain.country.repository.CountryRepository;
import com.souzip.domain.currency.dto.CurrencyResponse;
import com.souzip.domain.country.entity.Country;
import com.souzip.domain.currency.entity.Currency;
import com.souzip.global.exception.BusinessException;
import com.souzip.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CountryServiceCurrencyTest {

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CurrencyService currencyService;

    private Country createCountry(String code, Currency currency) {
        return Country.of(
                "South Korea",
                "대한민국",
                code,
                "Seoul",
                null,
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
    @DisplayName("국가 코드로 통화 정보를 조회할 수 있다")
    void getCurrencyByCountryCode_success() {
        // given
        Currency krw = createCurrency("KRW", "₩");
        Country korea = createCountry("KR", krw);
        given(countryRepository.findByCode("KR")).willReturn(Optional.of(korea));

        // when
        CurrencyResponse response = currencyService.getCurrencyByCountryCode("KR");

        // then
        assertThat(response.code()).isEqualTo("KRW");
        assertThat(response.symbol()).isEqualTo("₩");
    }

    @Test
    @DisplayName("존재하지 않는 국가 코드로 조회 시 예외가 발생한다.")
    void getCurrencyByCountryCode_countryNotFound() {
        // given
        given(countryRepository.findByCode("XX")).willReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> currencyService.getCurrencyByCountryCode("XX")
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COUNTRY_NOT_FOUND);
    }
}
