package com.souzip.api.domain.exchange_rate.service;

import com.souzip.api.domain.exchange_rate.dto.ExchangeRateExternalDto;
import com.souzip.api.domain.exchange_rate.entity.ExchangeRate;
import com.souzip.api.domain.exchange_rate.repository.ExchangeRateRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final RestTemplate restTemplate;

    @Value("${external.api.exchange-rate-base-url}")
    private String baseUrl;

    @Transactional
    public void fetchAndSaveExchangeRates() {
        ExchangeRateExternalDto externalDto = fetchFromExternalApi("KRW");

        Set<String> existingPairs = getExistingCurrencyPairs();
        List<ExchangeRate> newRates = extractNewRates(externalDto, existingPairs);

        saveIfNotEmpty(newRates);
    }

    private ExchangeRateExternalDto fetchFromExternalApi(String baseCurrency) {
        String url = String.format("%s/%s", baseUrl, baseCurrency);
        ExchangeRateExternalDto response = restTemplate.getForObject(url, ExchangeRateExternalDto.class);

        if (response == null || response.toEntities().isEmpty()) {
            log.warn("외부 API로부터 환율 데이터를 받지 못했습니다.");
            return ExchangeRateExternalDto.ofMultiple(Map.of(), baseCurrency);
        }
        return response;
    }

    private Set<String> getExistingCurrencyPairs() {
        return exchangeRateRepository.findAll()
                .stream()
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
