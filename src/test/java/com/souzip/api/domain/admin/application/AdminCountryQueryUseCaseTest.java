package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.port.CountryQueryPort;
import com.souzip.api.domain.admin.application.port.CountryQueryPort.CountryQueryResult;
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
class AdminCountryQueryUseCaseTest {

    @Mock
    private CountryQueryPort countryQueryPort;

    @InjectMocks
    private AdminCountryQueryUseCase adminCountryQueryUseCase;

    @DisplayName("나라 목록 조회 성공")
    @Test
    void getCountries_success() {
        // given
        List<CountryQueryResult> expected = List.of(
            new CountryQueryResult(1L, "대한민국"),
            new CountryQueryResult(2L, "일본")
        );

        given(countryQueryPort.getCountries()).willReturn(expected);

        // when
        List<CountryQueryResult> results = adminCountryQueryUseCase.getCountries();

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).nameKr()).isEqualTo("대한민국");
        assertThat(results.get(1).nameKr()).isEqualTo("일본");

        verify(countryQueryPort).getCountries();
    }
}
