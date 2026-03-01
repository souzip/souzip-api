package com.souzip.api.adapter.integration.googleplaces;

import com.souzip.api.adapter.integration.googleplaces.dto.GooglePlacesSearchResponse;
import com.souzip.api.adapter.integration.googleplaces.dto.GooglePlacesSearchResponse.Result;
import com.souzip.api.application.search.dto.SearchPlace;
import com.souzip.api.application.search.required.PlaceSearchProvider;
import com.souzip.api.domain.shared.Coordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class GooglePlacesAdapter implements PlaceSearchProvider {

    private static final int MAX_RESULTS = 10;

    private final RestTemplate restTemplate;

    @Value("${google.maps.places-text-search-url}")
    private String baseUrl;

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Value("${google.maps.language}")
    private String language;

    @Override
    public List<SearchPlace> searchByKeyword(String keyword) {
        try {
            GooglePlacesSearchResponse response = callPlacesApi(keyword);

            if (isInvalidResponse(response)) {
                return List.of();
            }

            return convertToPlaces(response.results());

        } catch (Exception e) {
            log.error("Places API 호출 실패 keyword={}", keyword, e);
            return List.of();
        }
    }

    private GooglePlacesSearchResponse callPlacesApi(String keyword) {
        String url = buildApiUrl(keyword);
        return restTemplate.getForObject(url, GooglePlacesSearchResponse.class);
    }

    private String buildApiUrl(String keyword) {
        return String.format(
                "%s?query=%s&language=%s&key=%s",
                baseUrl,
                keyword,
                language,
                apiKey
        );
    }

    private boolean isInvalidResponse(GooglePlacesSearchResponse response) {
        return response == null
                || response.results() == null
                || response.results().isEmpty();
    }

    private List<SearchPlace> convertToPlaces(List<Result> results) {
        return results.stream()
                .limit(MAX_RESULTS)
                .map(this::convertToPlace)
                .toList();
    }

    private SearchPlace convertToPlace(Result result) {
        return new SearchPlace(
                result.name(),
                result.formattedAddress(),
                Coordinate.of(
                        BigDecimal.valueOf(result.geometry().location().lat()),
                        BigDecimal.valueOf(result.geometry().location().lng())
                )
        );
    }
}
