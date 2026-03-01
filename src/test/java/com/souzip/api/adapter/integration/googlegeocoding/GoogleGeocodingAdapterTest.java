package com.souzip.api.adapter.integration.googlegeocoding;

import com.souzip.api.adapter.integration.googlegeocoding.dto.GoogleGeocodingResponse;
import com.souzip.api.application.geocoding.dto.GeocodingResult;
import com.souzip.api.domain.shared.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleGeocodingAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GoogleGeocodingAdapter adapter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adapter, "baseUrl", "https://maps.googleapis.com/maps/api/geocode/json");
        ReflectionTestUtils.setField(adapter, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(adapter, "language", "ko");
    }

    @DisplayName("정상적인 주소 정보를 반환한다")
    @Test
    void getAddress_Success() {
        // given
        Coordinate coordinate = Coordinate.of(
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        );

        GoogleGeocodingResponse mockResponse = createMockResponse();
        when(restTemplate.getForObject(anyString(), eq(GoogleGeocodingResponse.class)))
                .thenReturn(mockResponse);

        // when
        GeocodingResult result = adapter.getAddress(coordinate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.address()).isEqualTo("서울특별시 대한민국");
        assertThat(result.city()).isEqualTo("서울특별시");
        assertThat(result.countryCode()).isEqualTo("KR");
    }

    @DisplayName("locality가 없으면 sublocality를 city로 사용한다")
    @Test
    void getAddress_WithSublocality() {
        // given
        Coordinate coordinate = Coordinate.of(
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        );

        GoogleGeocodingResponse mockResponse = createMockResponseWithSublocality();
        when(restTemplate.getForObject(anyString(), eq(GoogleGeocodingResponse.class)))
                .thenReturn(mockResponse);

        // when
        GeocodingResult result = adapter.getAddress(coordinate);

        // then
        assertThat(result.city()).isEqualTo("강남구");
    }

    @DisplayName("응답이 null이면 빈 결과를 반환한다")
    @Test
    void getAddress_NullResponse() {
        // given
        Coordinate coordinate = Coordinate.of(
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        );

        when(restTemplate.getForObject(anyString(), eq(GoogleGeocodingResponse.class)))
                .thenReturn(null);

        // when
        GeocodingResult result = adapter.getAddress(coordinate);

        // then
        assertThat(result).isEqualTo(GeocodingResult.empty());
    }

    @DisplayName("결과가 비어있으면 빈 결과를 반환한다")
    @Test
    void getAddress_EmptyResults() {
        // given
        Coordinate coordinate = Coordinate.of(
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        );

        GoogleGeocodingResponse emptyResponse = new GoogleGeocodingResponse(List.of(), "OK");
        when(restTemplate.getForObject(anyString(), eq(GoogleGeocodingResponse.class)))
                .thenReturn(emptyResponse);

        // when
        GeocodingResult result = adapter.getAddress(coordinate);

        // then
        assertThat(result).isEqualTo(GeocodingResult.empty());
    }

    @DisplayName("API 호출 실패 시 빈 결과를 반환한다")
    @Test
    void getAddress_ApiCallFails() {
        // given
        Coordinate coordinate = Coordinate.of(
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        );

        when(restTemplate.getForObject(anyString(), eq(GoogleGeocodingResponse.class)))
                .thenThrow(new RuntimeException("API Error"));

        // when
        GeocodingResult result = adapter.getAddress(coordinate);

        // then
        assertThat(result).isEqualTo(GeocodingResult.empty());
    }

    private GoogleGeocodingResponse createMockResponse() {
        GoogleGeocodingResponse.AddressComponent country = new GoogleGeocodingResponse.AddressComponent(
                List.of("country", "political"),
                "대한민국",
                "KR"
        );

        GoogleGeocodingResponse.AddressComponent locality = new GoogleGeocodingResponse.AddressComponent(
                List.of("locality", "political"),
                "서울특별시",
                "서울특별시"
        );

        GoogleGeocodingResponse.Result result = new GoogleGeocodingResponse.Result(
                List.of(locality, country),
                "서울특별시 대한민국"
        );

        return new GoogleGeocodingResponse(List.of(result), "OK");
    }

    private GoogleGeocodingResponse createMockResponseWithSublocality() {
        GoogleGeocodingResponse.AddressComponent country = new GoogleGeocodingResponse.AddressComponent(
                List.of("country", "political"),
                "대한민국",
                "KR"
        );

        GoogleGeocodingResponse.AddressComponent sublocality = new GoogleGeocodingResponse.AddressComponent(
                List.of("sublocality_level_1", "sublocality", "political"),
                "강남구",
                "강남구"
        );

        GoogleGeocodingResponse.Result result = new GoogleGeocodingResponse.Result(
                List.of(sublocality, country),
                "대한민국 서울특별시 강남구"
        );

        return new GoogleGeocodingResponse(List.of(result), "OK");
    }
}
