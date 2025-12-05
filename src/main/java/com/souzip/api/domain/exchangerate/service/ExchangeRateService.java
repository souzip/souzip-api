package com.souzip.api.domain.exchangerate.service;

import com.souzip.api.domain.country.dto.CountryResponseDto;
import com.souzip.api.domain.country.service.CountryService;
import com.souzip.api.domain.exchangerate.client.ExchangeRateExternalApiClient;
import com.souzip.api.domain.exchangerate.dto.ExchangeRateExternalDto;
import com.souzip.api.domain.exchangerate.dto.ExchangeRateListResponse;
import com.souzip.api.domain.exchangerate.dto.ExchangeRateResponseDto;
import com.souzip.api.domain.exchangerate.entity.ExchangeRate;
import com.souzip.api.domain.exchangerate.repository.ExchangeRateRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ExchangeRateService {

    private static final String DEFAULT_BASE_CURRENCY = "KRW";

    private final ExchangeRateRepository exchangeRateRepository;
    private final CountryService countryService;
    private final ExchangeRateExternalApiClient apiClient;

    public ExchangeRateResponseDto getRateByCountry(String countryCode) {
        CountryResponseDto country = countryService.getCountryByCode(countryCode);
        return getRate(country.currency().code());
    }

    public ExchangeRateListResponse getRatesByCountries(Set<String> countryCodes) {
        List<ExchangeRateResponseDto> list = resolveRates(countryCodes);
        return ExchangeRateListResponse.from(list);
    }

    private List<ExchangeRateResponseDto> resolveRates(Set<String> countryCodes) {
        if (isEmpty(countryCodes)) {
            return getRatesInternal(null);
        }
        return getRatesInternal(mapCountriesToCurrencyCodes(countryCodes));
    }

    public ExchangeRateResponseDto getRate(String currencyCode) {
        return getRatesInternal(Set.of(currencyCode)).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.EXCHANGE_RATE_NOT_FOUND));
    }

    private List<ExchangeRateResponseDto> getRatesInternal(Set<String> currencyCodes) {
        return exchangeRateRepository.findAll().stream()
                .filter(rate -> rate.getBaseCode().equals(DEFAULT_BASE_CURRENCY)
                        && (currencyCodes == null || currencyCodes.contains(rate.getCurrencyCode())))
                .map(ExchangeRateResponseDto::from)
                .toList();
    }

    private Set<String> mapCountriesToCurrencyCodes(Set<String> countryCodes) {
        return countryCodes.stream()
                .map(code -> countryService.getCountryByCode(code).currency().code())
                .collect(Collectors.toSet());
    }

    private boolean isEmpty(Set<?> set) {
        return set == null || set.isEmpty();
    }

    @Transactional
    public void fetchAndSaveExchangeRates() {
        ExchangeRateExternalDto externalDto = apiClient.fetchRates();
        Set<String> existingPairs = getExistingCurrencyPairs();
        List<ExchangeRate> newRates = extractNewRates(externalDto, existingPairs);

        saveIfNotEmpty(newRates);
    }

    private Set<String> getExistingCurrencyPairs() {
        return exchangeRateRepository.findAll().stream()
                .map(this::toPairKey)
                .collect(Collectors.toSet());
    }

    private List<ExchangeRate> extractNewRates(ExchangeRateExternalDto externalDto, Set<String> existingPairs) {
        return externalDto.toEntities().stream()
                .filter(rate -> isNewRate(rate, existingPairs))
                .toList();
    }

    private boolean isNewRate(ExchangeRate rate, Set<String> existingPairs) {
        return !existingPairs.contains(toPairKey(rate));
    }

    @Transactional
    public void saveIfNotEmpty(List<ExchangeRate> rates) {
        if (rates.isEmpty()) {
            log.info("저장할 새로운 환율 데이터가 없습니다.");
            return;
        }

        exchangeRateRepository.saveAll(rates);
        log.info("환율 데이터 저장 완료: {}건", rates.size());
    }

    private String toPairKey(ExchangeRate rate) {
        return rate.getCurrencyCode() + "->" + rate.getBaseCode();
    }
}
