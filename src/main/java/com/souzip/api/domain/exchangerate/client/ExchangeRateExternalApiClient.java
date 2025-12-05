package com.souzip.api.domain.exchangerate.client;

import com.souzip.api.domain.exchangerate.dto.ExchangeRateExternalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExchangeRateExternalApiClient {

    private final RestTemplate restTemplate;

    @Value("${external.api.exchange-rate-base-url}")
    private String baseUrl;

    private static final String DEFAULT_BASE_CURRENCY = "KRW";

    public ExchangeRateExternalDto fetchRates() {
        String url = baseUrl + "/" + DEFAULT_BASE_CURRENCY;
        ExchangeRateExternalDto response = restTemplate.getForObject(url, ExchangeRateExternalDto.class);

        if (response == null || response.toEntities().isEmpty()) {
            log.warn("외부 API로부터 환율 데이터를 받지 못했습니다. (baseCurrency: {})", DEFAULT_BASE_CURRENCY);
            return ExchangeRateExternalDto.ofMultiple(Map.of(), DEFAULT_BASE_CURRENCY);
        }

        return response;
    }
}
