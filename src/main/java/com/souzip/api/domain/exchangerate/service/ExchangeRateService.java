package com.souzip.api.domain.exchangerate.service;

import com.souzip.api.domain.country.dto.CountryResponseDto;
import com.souzip.api.domain.country.service.CountryService;
import com.souzip.api.domain.exchangerate.client.ExchangeRateExternalApiClient;
import com.souzip.api.domain.exchangerate.dto.ExchangeCalculatedPrice;
import com.souzip.api.domain.exchangerate.dto.ExchangeRateExternalDto;
import com.souzip.api.domain.exchangerate.dto.ExchangeRateResponseDto;
import com.souzip.api.domain.exchangerate.entity.ExchangeRate;
import com.souzip.api.domain.exchangerate.repository.ExchangeRateRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ExchangeRateService {

    private static final String DEFAULT_BASE_CURRENCY = "KRW";

    private final ExchangeRateRepository exchangeRateRepository;
    private final CountryService countryService;
    private final ExchangeRateExternalApiClient apiClient;

    public ExchangeCalculatedPrice calculatePrice(
            String countryCode,
            Integer localPrice,
            Integer krwPrice
    ) {
        CountryResponseDto country = countryService.getCountryByCode(countryCode);
        ExchangeRateResponseDto rate = getRate(country.currency().code());

        if (localPrice != null) {
            return fromLocalPrice(localPrice, rate, country);
        }

        if (krwPrice != null) {
            return fromKrwPrice(krwPrice, rate, country);
        }

        return new ExchangeCalculatedPrice(
                0,
                0,
                country.currency().symbol()
        );
    }

    private ExchangeCalculatedPrice fromLocalPrice(
            Integer localPrice,
            ExchangeRateResponseDto rate,
            CountryResponseDto country
    ) {
        int krw = multiply(localPrice, rate.rate());

        return new ExchangeCalculatedPrice(
                localPrice,
                krw,
                country.currency().symbol()
        );
    }

    private ExchangeCalculatedPrice fromKrwPrice(
            Integer krwPrice,
            ExchangeRateResponseDto rate,
            CountryResponseDto country
    ) {
        int local = divide(krwPrice, rate.rate());

        return new ExchangeCalculatedPrice(
                local,
                krwPrice,
                country.currency().symbol()
        );
    }

    private int multiply(Integer value, BigDecimal rate) {
        return BigDecimal.valueOf(value)
                .multiply(rate)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private int divide(Integer value, BigDecimal rate) {
        return BigDecimal.valueOf(value)
                .divide(rate, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    public ExchangeRateResponseDto getRate(String currencyCode) {
        ExchangeRate rate = exchangeRateRepository
                .findByCurrencyCodeAndBaseCode(currencyCode, DEFAULT_BASE_CURRENCY)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXCHANGE_RATE_NOT_FOUND));

        return ExchangeRateResponseDto.from(rate);
    }

    @Transactional
    public void fetchAndSaveExchangeRates() {
        ExchangeRateExternalDto externalDto = apiClient.fetchRates();

        List<ExchangeRate> rates = externalDto.toEntities();
        if (rates.isEmpty()) {
            log.info("저장할 환율 데이터가 없습니다.");
            return;
        }

        rates.forEach(this::saveOrUpdate);
        log.info("환율 데이터 갱신 완료: {}건", rates.size());
    }

    private void saveOrUpdate(ExchangeRate newRate) {
        exchangeRateRepository
                .findByCurrencyCodeAndBaseCode(
                        newRate.getCurrencyCode(),
                        newRate.getBaseCode()
                )
                .ifPresentOrElse(
                        existing -> existing.updateRate(newRate.getRate()),
                        () -> exchangeRateRepository.save(newRate)
                );
    }
}
