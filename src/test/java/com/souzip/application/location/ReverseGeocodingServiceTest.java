package com.souzip.application.location;

import com.souzip.application.location.dto.AddressResult;
import com.souzip.application.location.required.AddressProvider;
import com.souzip.domain.shared.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReverseGeocodingServiceTest {

    @InjectMocks
    private ReverseGeocodingService geocodingService;

    @Mock
    private AddressProvider addressProvider;

    @DisplayName("좌표를 입력받아 주소 정보를 반환한다")
    @Test
    void getAddress() {
        // given
        Coordinate coordinate = Coordinate.of(
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        );

        AddressResult expected = new AddressResult(
                "110 Sejong-daero, Jung District, Seoul, South Korea",
                "Seoul",
                "KR"
        );

        given(addressProvider.getAddress(any(Coordinate.class)))
                .willReturn(expected);

        // when
        AddressResult result = geocodingService.getAddress(coordinate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.address()).isEqualTo("110 Sejong-daero, Jung District, Seoul, South Korea");
        assertThat(result.city()).isEqualTo("Seoul");
        assertThat(result.countryCode()).isEqualTo("KR");

        verify(addressProvider).getAddress(coordinate);
    }

    @DisplayName("주소 정보가 없는 경우 빈 결과를 반환한다")
    @Test
    void getAddress_EmptyResult() {
        // given
        Coordinate coordinate = Coordinate.of(
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        AddressResult emptyResult = AddressResult.empty();

        given(addressProvider.getAddress(any(Coordinate.class)))
                .willReturn(emptyResult);

        // when
        AddressResult result = geocodingService.getAddress(coordinate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.address()).isNull();
        assertThat(result.city()).isNull();
        assertThat(result.countryCode()).isNull();

        verify(addressProvider).getAddress(coordinate);
    }
}
