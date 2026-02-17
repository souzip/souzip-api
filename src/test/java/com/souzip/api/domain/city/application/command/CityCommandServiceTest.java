package com.souzip.api.domain.city.application.command;

import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.event.CityCreatedEvent;
import com.souzip.api.domain.city.event.CityDeletedEvent;
import com.souzip.api.domain.city.event.CityPriorityUpdatedEvent;
import com.souzip.api.domain.city.repository.CityRepository;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.repository.CountryRepository;
import com.souzip.api.global.exception.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CityCommandServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CityCommandService cityCommandService;

    @DisplayName("도시 우선순위 설정 성공 - 기존 우선순위 없음")
    @Test
    void updateCityPriority_withNoPreviousPriority_success() {
        // given
        Long cityId = 1L;
        Integer newPriority = 1;

        Country country = mock(Country.class);
        given(country.getId()).willReturn(1L);

        City city = City.create("Seoul", "서울",
            BigDecimal.valueOf(37.56), BigDecimal.valueOf(126.97), country);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));
        given(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, newPriority))
            .willReturn(List.of());

        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(cityId, newPriority);

        // when
        cityCommandService.updateCityPriority(command);

        // then
        verify(cityRepository).findByIdWithLock(cityId);
        verify(cityRepository, never()).findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, newPriority + 1);
        verify(cityRepository).findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, newPriority);
        verify(eventPublisher).publishEvent(argThat(event ->
            event instanceof CityPriorityUpdatedEvent &&
                ((CityPriorityUpdatedEvent) event).cityId().equals(cityId) &&
                ((CityPriorityUpdatedEvent) event).oldPriority() == null &&
                ((CityPriorityUpdatedEvent) event).newPriority().equals(newPriority)
        ));
        assertThat(city.getPriority()).isEqualTo(newPriority);
    }

    @DisplayName("도시 우선순위 설정 성공 - 기존 우선순위 있음")
    @Test
    void updateCityPriority_withPreviousPriority_success() {
        // given
        Long cityId = 1L;
        Integer oldPriority = 1;
        Integer newPriority = 3;

        Country country = mock(Country.class);
        given(country.getId()).willReturn(1L);

        City city = City.create("Seoul", "서울",
            BigDecimal.valueOf(37.56), BigDecimal.valueOf(126.97), country);
        city.updatePriority(oldPriority);

        City existingCity = City.create("Busan", "부산",
            BigDecimal.valueOf(35.10), BigDecimal.valueOf(129.03), country);
        existingCity.updatePriority(newPriority);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));
        given(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, oldPriority + 1))
            .willReturn(List.of());
        given(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, newPriority))
            .willReturn(List.of(existingCity));

        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(cityId, newPriority);

        // when
        cityCommandService.updateCityPriority(command);

        // then
        verify(cityRepository).findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, oldPriority + 1);
        verify(cityRepository).findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, newPriority);
        verify(eventPublisher).publishEvent(argThat(event ->
            event instanceof CityPriorityUpdatedEvent &&
                ((CityPriorityUpdatedEvent) event).cityId().equals(cityId) &&
                ((CityPriorityUpdatedEvent) event).oldPriority().equals(oldPriority) &&
                ((CityPriorityUpdatedEvent) event).newPriority().equals(newPriority)
        ));
        assertThat(existingCity.getPriority()).isEqualTo(4);
        assertThat(city.getPriority()).isEqualTo(newPriority);
    }

    @DisplayName("도시 우선순위 초기화 성공 - null로 설정")
    @Test
    void updateCityPriority_resetToNull_success() {
        // given
        Long cityId = 1L;
        Integer oldPriority = 1;

        Country country = mock(Country.class);
        given(country.getId()).willReturn(1L);

        City city = City.create("Seoul", "서울",
            BigDecimal.valueOf(37.56), BigDecimal.valueOf(126.97), country);
        city.updatePriority(oldPriority);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));
        given(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, oldPriority + 1))
            .willReturn(List.of());

        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(cityId, null);

        // when
        cityCommandService.updateCityPriority(command);

        // then
        verify(cityRepository).findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, oldPriority + 1);
        verify(cityRepository, never()).findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, null);
        verify(eventPublisher).publishEvent(argThat(event ->
            event instanceof CityPriorityUpdatedEvent &&
                ((CityPriorityUpdatedEvent) event).cityId().equals(cityId) &&
                ((CityPriorityUpdatedEvent) event).oldPriority().equals(oldPriority) &&
                ((CityPriorityUpdatedEvent) event).newPriority() == null
        ));
        assertThat(city.getPriority()).isNull();
    }

    @DisplayName("연속 구간만 당겨지고 gap 이후는 유지된다")
    @Test
    void updateCityPriority_onlyPullsContiguousRange() {
        // given
        Long cityId = 1L;
        Integer oldPriority = 3;
        Integer newPriority = 5;

        Country country = mock(Country.class);
        given(country.getId()).willReturn(1L);

        City city = City.create("Seoul", "서울",
            BigDecimal.valueOf(37.56), BigDecimal.valueOf(126.97), country);
        city.updatePriority(oldPriority);

        City city2 = City.create("Busan", "부산",
            BigDecimal.valueOf(35.10), BigDecimal.valueOf(129.03), country);
        city2.updatePriority(4);

        City city3 = City.create("Jeju", "제주",
            BigDecimal.valueOf(33.49), BigDecimal.valueOf(126.53), country);
        city3.updatePriority(100);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));
        given(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, oldPriority + 1))
            .willReturn(List.of(city2, city3));
        given(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, newPriority))
            .willReturn(List.of(city3));

        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(cityId, newPriority);

        // when
        cityCommandService.updateCityPriority(command);

        // then
        assertThat(city2.getPriority()).isEqualTo(3);
        assertThat(city3.getPriority()).isEqualTo(100);
        assertThat(city.getPriority()).isEqualTo(newPriority);
        verify(eventPublisher).publishEvent(any(CityPriorityUpdatedEvent.class));
    }

    @DisplayName("연속 구간만 밀리고 gap 이후는 유지된다")
    @Test
    void updateCityPriority_onlyShiftsContiguousRange() {
        // given
        Long cityId = 1L;
        Integer newPriority = 2;

        Country country = mock(Country.class);
        given(country.getId()).willReturn(1L);

        City city = City.create("Seoul", "서울",
            BigDecimal.valueOf(37.56), BigDecimal.valueOf(126.97), country);

        City city2 = City.create("Busan", "부산",
            BigDecimal.valueOf(35.10), BigDecimal.valueOf(129.03), country);
        city2.updatePriority(2);

        City city3 = City.create("Jeju", "제주",
            BigDecimal.valueOf(33.49), BigDecimal.valueOf(126.53), country);
        city3.updatePriority(100);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));
        given(cityRepository.findByCountryIdAndPriorityGoeOrderByPriorityAsc(1L, newPriority))
            .willReturn(List.of(city2, city3));

        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(cityId, newPriority);

        // when
        cityCommandService.updateCityPriority(command);

        // then
        assertThat(city2.getPriority()).isEqualTo(3);
        assertThat(city3.getPriority()).isEqualTo(100);
        assertThat(city.getPriority()).isEqualTo(newPriority);
        verify(eventPublisher).publishEvent(any(CityPriorityUpdatedEvent.class));
    }

    @DisplayName("존재하지 않는 도시 우선순위 설정 시 예외 발생")
    @Test
    void updateCityPriority_cityNotFound_throwsException() {
        // given
        Long cityId = 999L;
        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.empty());

        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(cityId, 1);

        // when & then
        assertThatThrownBy(() -> cityCommandService.updateCityPriority(command))
            .isInstanceOf(BusinessException.class);

        verify(cityRepository, never()).findByCountryIdAndPriorityGoeOrderByPriorityAsc(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @DisplayName("도시 생성 성공")
    @Test
    void createCity_success() {
        // given
        Country country = mock(Country.class);
        given(country.getId()).willReturn(1L);
        given(countryRepository.findById(1L)).willReturn(Optional.of(country));

        CreateCityCommand command = new CreateCityCommand(
            "Seoul", "서울", 37.56, 126.97, 1L
        );

        // when
        cityCommandService.createCity(command);

        // then
        verify(countryRepository).findById(1L);
        verify(cityRepository).save(any(City.class));
        verify(eventPublisher).publishEvent(argThat(event ->
            event instanceof CityCreatedEvent &&
                ((CityCreatedEvent) event).countryId().equals(1L)
        ));
    }

    @DisplayName("도시 생성 실패 - 나라 없음")
    @Test
    void createCity_countryNotFound_throwsException() {
        // given
        given(countryRepository.findById(999L)).willReturn(Optional.empty());

        CreateCityCommand command = new CreateCityCommand(
            "Seoul", "서울", 37.56, 126.97, 999L
        );

        // when & then
        assertThatThrownBy(() -> cityCommandService.createCity(command))
            .isInstanceOf(BusinessException.class);

        verify(cityRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @DisplayName("도시 삭제 성공")
    @Test
    void deleteCity_success() {
        // given
        Long cityId = 1L;
        Country country = mock(Country.class);
        City city = City.create("Seoul", "서울",
            BigDecimal.valueOf(37.56), BigDecimal.valueOf(126.97), country);

        given(cityRepository.findById(cityId)).willReturn(Optional.of(city));

        DeleteCityCommand command = new DeleteCityCommand(cityId);

        // when
        cityCommandService.deleteCity(command);

        // then
        verify(cityRepository).findById(cityId);
        verify(cityRepository).delete(city);
        verify(eventPublisher).publishEvent(argThat(event ->
            event instanceof CityDeletedEvent &&
                ((CityDeletedEvent) event).cityId().equals(cityId)
        ));
    }

    @DisplayName("도시 삭제 실패 - 도시 없음")
    @Test
    void deleteCity_cityNotFound_throwsException() {
        // given
        given(cityRepository.findById(999L)).willReturn(Optional.empty());

        DeleteCityCommand command = new DeleteCityCommand(999L);

        // when & then
        assertThatThrownBy(() -> cityCommandService.deleteCity(command))
            .isInstanceOf(BusinessException.class);

        verify(cityRepository, never()).delete(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
