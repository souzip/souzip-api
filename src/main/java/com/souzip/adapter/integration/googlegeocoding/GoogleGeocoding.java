package com.souzip.adapter.integration.googlegeocoding;

import com.souzip.adapter.integration.googlegeocoding.dto.GoogleGeocodingResponse;
import com.souzip.adapter.integration.googlegeocoding.dto.GoogleGeocodingResponse.AddressComponent;
import com.souzip.adapter.integration.googlegeocoding.dto.GoogleGeocodingResponse.Result;
import com.souzip.application.location.dto.AddressResult;
import com.souzip.application.location.required.AddressProvider;
import com.souzip.domain.shared.Coordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
@Component
public class GoogleGeocoding implements AddressProvider {

    private static final String TYPE_COUNTRY = "country";
    private static final String TYPE_LOCALITY = "locality";
    private static final String TYPE_SUBLOCALITY_PREFIX = "sublocality";

    private final RestTemplate restTemplate;

    @Value("${google.maps.geocoding-base-url}")
    private String baseUrl;

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Value("${google.maps.language}")
    private String language;


    @Override
    public AddressResult getAddress(Coordinate coordinate) {
        try {
            GoogleGeocodingResponse response = callGeocodingApi(coordinate);

            if (isInvalidResponse(response)) {
                return AddressResult.empty();
            }

            return parseToGeocodingResult(response.results());

        } catch (Exception e) {
            log.error("Geocoding API 호출 실패 coordinate={}", coordinate, e);
            return AddressResult.empty();
        }
    }

    private GoogleGeocodingResponse callGeocodingApi(Coordinate coordinate) {
        String url = buildApiUrl(coordinate);
        return restTemplate.getForObject(url, GoogleGeocodingResponse.class);
    }

    private String buildApiUrl(Coordinate coordinate) {
        return String.format(
                "%s?latlng=%s,%s&language=%s&key=%s",
                baseUrl,
                coordinate.getLatitude().doubleValue(),
                coordinate.getLongitude().doubleValue(),
                language,
                apiKey
        );
    }

    private boolean isInvalidResponse(GoogleGeocodingResponse response) {
        return response == null
                || response.results() == null
                || response.results().isEmpty();
    }

    private AddressResult parseToGeocodingResult(List<Result> results) {
        return new AddressResult(
                extractFormattedAddress(results),
                extractCity(results),
                extractCountryCode(results)
        );
    }

    private String extractFormattedAddress(List<Result> results) {
        return results.stream()
                .map(Result::formattedAddress)
                .filter(addr -> addr != null && !addr.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String extractCountryCode(List<Result> results) {
        return findAddressComponent(
                results,
                component -> component.types().contains(TYPE_COUNTRY)
        ).map(AddressComponent::shortName)
                .orElse(null);
    }

    private String extractCity(List<Result> results) {
        return findAddressComponent(
                results,
                component -> component.types().contains(TYPE_LOCALITY)
        ).map(AddressComponent::longName)
                .or(() -> findAddressComponent(
                        results,
                        component -> component.types().stream()
                                .anyMatch(type -> type.startsWith(TYPE_SUBLOCALITY_PREFIX))
                ).map(AddressComponent::longName))
                .orElse(null);
    }

    private java.util.Optional<AddressComponent> findAddressComponent(
            List<Result> results,
            Predicate<AddressComponent> predicate
    ) {
        return results.stream()
                .filter(result -> result.addressComponents() != null)
                .flatMap(result -> result.addressComponents().stream())
                .filter(predicate)
                .findFirst();
    }
}
