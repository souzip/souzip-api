package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.port.CityQueryPort;
import com.souzip.api.domain.admin.application.port.CityQueryPort.CityQueryResult;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminCityQueryUseCaseTest {

    @Mock
    private CityQueryPort cityQueryPort;

    @InjectMocks
    private AdminCityQueryUseCase adminCityQueryUseCase;

    @DisplayName("나라 ID로 도시 목록 조회 성공")
    @Test
    void getCities_success() {
        // given
        Long countryId = 1L;
        LocalDateTime now = LocalDateTime.now();

        List<CityQueryResult> expected = List.of(
            new CityQueryResult(1L, "서울", 1, now),
            new CityQueryResult(2L, "부산", 2, now)
        );

        given(cityQueryPort.getCities(countryId)).willReturn(expected);

        // when
        List<CityQueryResult> results = adminCityQueryUseCase.getCities(countryId);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).nameKr()).isEqualTo("서울");
        assertThat(results.get(0).priority()).isEqualTo(1);
        assertThat(results.get(1).nameKr()).isEqualTo("부산");
        assertThat(results.get(1).priority()).isEqualTo(2);

        verify(cityQueryPort).getCities(countryId);
    }
}
