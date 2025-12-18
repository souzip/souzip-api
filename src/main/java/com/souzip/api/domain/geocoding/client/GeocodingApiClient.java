package com.souzip.api.domain.geocoding.client;

import com.souzip.api.domain.geocoding.dto.GeocodingExternalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class GeocodingApiClient {

    private final RestTemplate restTemplate;

    @Value("${external.api.geocoding-base-url}")
    private String baseUrl;

    @Value("${external.api.geocoding-key}")
    private String apiKey;

    public GeocodingExternalDto getAddress(double latitude, double longitude) {
        String url = String.format(
                "%s?latlng=%s,%s&language=en&key=%s",
                baseUrl,
                latitude,
                longitude,
                apiKey
        );
        try {
            return restTemplate.getForObject(url, GeocodingExternalDto.class);
        } catch (Exception e) {
            log.error("Geocoding API 호출 실패 lat={}, lng={}", latitude, longitude, e);
            return null;
        }
    }
}
