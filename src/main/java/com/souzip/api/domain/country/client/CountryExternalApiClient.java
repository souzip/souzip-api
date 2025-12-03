package com.souzip.api.domain.country.client;

import com.souzip.api.domain.country.dto.CountryExternalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CountryExternalApiClient {

    private final RestTemplate restTemplate;

    @Value("${external.api.countries-base-url}")
    private String baseUrl;

    private static final String COUNTRIES_API_PATH = "/all?fields=name,capital,region,flags,cca2,latlng,translations,currencies";

    public List<CountryExternalDto> fetchCountries() {
        try {
            CountryExternalDto[] response = callCountriesApi();
            return convertToList(response);
        } catch (Exception e) {
            log.error("외부 API 호출 중 오류 발생", e);
            return List.of();
        }
    }

    private CountryExternalDto[] callCountriesApi() {
        String url = baseUrl + COUNTRIES_API_PATH;
        return restTemplate.getForObject(url, CountryExternalDto[].class);
    }

    private List<CountryExternalDto> convertToList(CountryExternalDto[] response) {
        if (isEmptyResponse(response)) {
            log.warn("외부 API로부터 국가 데이터를 받지 못했습니다.");
            return List.of();
        }
        return Arrays.asList(response);
    }

    private boolean isEmptyResponse(CountryExternalDto[] response) {
        return response == null || response.length == 0;
    }
}
