package com.souzip.api.domain.geocoding.service;

import com.souzip.api.domain.geocoding.client.GeocodingApiClient;
import com.souzip.api.domain.geocoding.dto.GeocodingAddressResponse;
import com.souzip.api.domain.geocoding.dto.GeocodingExternalDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final GeocodingApiClient geocodingApiClient;

    public GeocodingAddressResponse getAddress(double latitude, double longitude) {
        GeocodingExternalDto response =
                geocodingApiClient.getAddress(latitude, longitude);

        if (isInvalidResponse(response)) {
            return emptyResponse();
        }

        List<GeocodingExternalDto.Result> results = response.results();

        return new GeocodingAddressResponse(
                extractFormattedAddress(results),
                extractCity(results),
                extractCountryCode(results)
        );
    }

    private boolean isInvalidResponse(GeocodingExternalDto response) {
        return response == null || response.results() == null;
    }

    private GeocodingAddressResponse emptyResponse() {
        return new GeocodingAddressResponse(null, null, null);
    }

    private String extractFormattedAddress(List<GeocodingExternalDto.Result> results) {
        return results.stream()
                .map(GeocodingExternalDto.Result::formattedAddress)
                .filter(addr -> addr != null && !addr.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String extractCountryCode(List<GeocodingExternalDto.Result> results) {
        return results.stream()
                .filter(r -> r.addressComponents() != null)
                .flatMap(r -> r.addressComponents().stream())
                .filter(c -> c.types().contains("country"))
                .map(GeocodingExternalDto.AddressComponent::shortName)
                .findFirst()
                .orElse(null);
    }

    private String extractCity(List<GeocodingExternalDto.Result> results) {
        String locality = results.stream()
                .filter(r -> r.addressComponents() != null)
                .flatMap(r -> r.addressComponents().stream())
                .filter(c -> c.types().contains("locality"))
                .map(GeocodingExternalDto.AddressComponent::longName)
                .findFirst()
                .orElse(null);

        if (locality != null) return locality;

        return results.stream()
                .filter(r -> r.addressComponents() != null)
                .flatMap(r -> r.addressComponents().stream())
                .filter(c -> c.types().stream().anyMatch(t -> t.startsWith("sublocality")))
                .map(GeocodingExternalDto.AddressComponent::longName)
                .findFirst()
                .orElse(null);
    }
}
