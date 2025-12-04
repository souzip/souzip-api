package com.souzip.api.domain.exchange_rate.service;

import com.souzip.api.domain.country.dto.CountryResponseDto;
import com.souzip.api.domain.country.dto.RegionDto;
import com.souzip.api.domain.country.entity.Region;
import com.souzip.api.domain.country.service.CountryService;
import com.souzip.api.domain.currency.CurrencyDto;
import com.souzip.api.domain.currency.entity.Currency;
import com.souzip.api.domain.exchange_rate.dto.ExchangeRateExternalDto;
import com.souzip.api.domain.exchange_rate.dto.ExchangeRateResponseDto;
import com.souzip.api.domain.exchange_rate.entity.ExchangeRate;
import com.souzip.api.domain.exchange_rate.repository.ExchangeRateRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private CountryService countryService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    private ExchangeRate createRate(String currencyCode, String baseCode, BigDecimal rate) {
        return ExchangeRate.of(currencyCode, baseCode, rate);
    }

    @Nested
    @DisplayName("단일 국가 환율 조회")
    class GetRateByCountry {

        @Test
        @DisplayName("국가 코드로 환율 정보를 조회할 수 있다.")
        void getRateByCountry() {
            // given
            String countryCode = "US";
            RegionDto regionDto = RegionDto.from(Region.AMERICAS);
            CountryResponseDto countryDto = new CountryResponseDto(
                    "United States",
                    "미국",
                    "US",
                    regionDto,
                    "Washington",
                    "https://flagcdn.com/us.png",
                    BigDecimal.valueOf(38.9),
                    BigDecimal.valueOf(-77.0),
                    CurrencyDto.from(Currency.of("USD", "$"))
            );

            when(countryService.getCountryByCode(countryCode))
                    .thenReturn(countryDto);

            // 미국 달러(USD)가 한국 원(KRW) 기준으로 얼마인지 확인
            ExchangeRate rate = createRate("KRW", "USD", BigDecimal.valueOf(1300));
            when(exchangeRateRepository.findAll())
                    .thenReturn(List.of(rate));

            // when
            ExchangeRateResponseDto response = exchangeRateService.getRateByCountry(countryCode);

            // then
            assertThat(response).isNotNull();
            assertThat(response.currencyCode()).isEqualTo("USD");
            assertThat(response.rate()).isEqualTo(BigDecimal.valueOf(1300));
        }

        @Test
        @DisplayName("존재하지 않는 국가 코드로 환율을 조회하면 예외가 발생한다.")
        void fail_getRateByCountry_notFound() {
            // given
            String invalidCode = "ZZZ";
            when(countryService.getCountryByCode(invalidCode))
                    .thenThrow(new BusinessException(ErrorCode.COUNTRY_NOT_FOUND));

            // when
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> exchangeRateService.getRateByCountry(invalidCode)
            );

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COUNTRY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("여러 국가 환율 조회")
    class GetRatesByCountries {

        @Test
        @DisplayName("국가 코드를 전달하면 해당 국가들의 환율 리스트를 반환한다")
        void success_getRatesByCountries() {
            // given
            Set<String> codes = Set.of("US", "JP");

            when(countryService.getCountryByCode("US"))
                    .thenReturn(new CountryResponseDto(
                            "United States",
                            "미국",
                            "US",
                            RegionDto.from(Region.AMERICAS),
                            "Washington",
                            "https://flagcdn.com/us.png",
                            BigDecimal.valueOf(38.9),
                            BigDecimal.valueOf(-77.0),
                            CurrencyDto.from(Currency.of("USD", "$"))
                    ));

            when(countryService.getCountryByCode("JP"))
                    .thenReturn(new CountryResponseDto(
                            "Japan",
                            "일본",
                            "JP",
                            RegionDto.from(Region.ASIA),
                            "Tokyo",
                            "https://flagcdn.com/jp.png",
                            BigDecimal.valueOf(35.7),
                            BigDecimal.valueOf(139.7),
                            CurrencyDto.from(Currency.of("JPY", "¥"))
                    ));

            // baseCode = KRW (기준통화), currencyCode = 대상통화
            ExchangeRate usd = createRate("KRW", "USD", BigDecimal.valueOf(1300));
            ExchangeRate jpy = createRate("KRW", "JPY", BigDecimal.valueOf(9));

            when(exchangeRateRepository.findAll())
                    .thenReturn(List.of(usd, jpy));

            // when
            List<ExchangeRateResponseDto> results =
                    exchangeRateService.getRatesByCountries(codes);

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(ExchangeRateResponseDto::currencyCode)
                    .containsExactlyInAnyOrder("USD", "JPY");
        }

        @Test
        @DisplayName("국가 코드를 따로 전달하지 않으면 기본적으로는 전체 환율을 조회한다.")
        void success_getAllRates_whenNoCountryCode() {
            // given
            ExchangeRate usd = createRate("KRW", "USD", BigDecimal.valueOf(1300));
            ExchangeRate jpy = createRate("KRW", "JPY", BigDecimal.valueOf(9));
            ExchangeRate eur = createRate("KRW", "EUR", BigDecimal.valueOf(1500));

            when(exchangeRateRepository.findAll())
                    .thenReturn(List.of(usd, jpy, eur));

            // when
            List<ExchangeRateResponseDto> results =
                    exchangeRateService.getRatesByCountries(null); // null 전달 -> 전체 조회

            // then
            assertThat(results).hasSize(3);
            assertThat(results).extracting(ExchangeRateResponseDto::currencyCode)
                    .containsExactlyInAnyOrder("USD", "JPY", "EUR");
        }
    }

    @Nested
    @DisplayName("환율 정보 외부 API 호출")
    class FetchAndSaveExchangeRatesTest {

        private Map<String, Double> conversionRates = Map.of(
                "USD", 0.00077,
                "JPY", 0.1
        );

        private ExchangeRateExternalDto externalDto = ExchangeRateExternalDto.ofMultiple(conversionRates, "KRW");

        @BeforeEach
        void setup() {
            // given
            when(restTemplate.getForObject(anyString(), eq(ExchangeRateExternalDto.class)))
                    .thenReturn(externalDto);
        }

        @Test
        @DisplayName("외부 API가 정상적으로 호출된다")
        void apiCalled() {
            // when
            exchangeRateService.fetchAndSaveExchangeRates("KRW");

            // then
            verify(restTemplate).getForObject(contains("KRW"), eq(ExchangeRateExternalDto.class));
        }

        @Test
        @DisplayName("신규 환율만 DB에 저장된다")
        void saveOnlyNewRates() {
            // given
            ExchangeRate existingUsd = createRate("KRW", "USD", BigDecimal.valueOf(1300));
            when(exchangeRateRepository.findAll()).thenReturn(List.of(existingUsd));
            when(exchangeRateRepository.saveAll(anyList()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            exchangeRateService.fetchAndSaveExchangeRates("KRW");

            // then
            verify(exchangeRateRepository).saveAll(argThat((List<ExchangeRate> list) ->
                    list.size() == 1 &&
                            list.get(0).getBaseCode().equals("KRW") &&
                            list.get(0).getCurrencyCode().equals("JPY")
            ));
        }
    }

    @Nested
    @DisplayName("saveIfNotEmpty 동작 검증")
    class SaveNewExchangeRates {

        @Test
        @DisplayName("저장할 환율 데이터가 없으면 DB에 저장하지 않는다.")
        void skip_whenNoNewRates() {
            // given
            List<ExchangeRate> newRates = List.of();

            // when
            exchangeRateService.saveIfNotEmpty(newRates);

            // then
            verify(exchangeRateRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("저장할 환율 데이터가 있으면 DB에 저장한다.")
        void save_whenNewRatesExist() {
            // given
            ExchangeRate existingUsd = createRate("KRW", "USD", BigDecimal.valueOf(1300));
            lenient().when(exchangeRateRepository.findAll()).thenReturn(List.of(existingUsd));

            List<ExchangeRate> newRates = List.of(
                    createRate("KRW", "JPY", BigDecimal.valueOf(9))
            );

            // when
            exchangeRateService.saveIfNotEmpty(newRates);

            // then
            verify(exchangeRateRepository).saveAll(newRates);
        }
    }
}
