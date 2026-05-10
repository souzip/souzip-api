package com.souzip.domain.city.application.command;

import com.souzip.domain.city.entity.City;
import com.souzip.domain.city.event.CityCreatedEvent;
import com.souzip.domain.city.event.CityDeletedEvent;
import com.souzip.domain.city.event.CityPriorityUpdatedEvent;
import com.souzip.domain.city.repository.CityRepository;
import com.souzip.domain.city.service.CityPriorityDomainService;
import com.souzip.domain.country.entity.Country;
import com.souzip.domain.country.repository.CountryRepository;
import com.souzip.shared.exception.BusinessException;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private CityPriorityDomainService cityPriorityDomainService;

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
        ReflectionTestUtils.setField(city, "id", cityId);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));

        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(cityId, newPriority);

        // when
        cityCommandService.updateCityPriority(command);

        // then
        verify(cityRepository).findByIdWithLock(cityId);
        verify(cityPriorityDomainService).adjustPriorities(cityId, null, newPriority, 1L);
        verify(eventPublisher).publishEvent(any(CityPriorityUpdatedEvent.class));
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
        ReflectionTestUtils.setField(city, "id", cityId);
        city.updatePriority(oldPriority);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));

        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(cityId, newPriority);

        // when
        cityCommandService.updateCityPriority(command);

        // then
        verify(cityPriorityDomainService).adjustPriorities(cityId, oldPriority, newPriority, 1L);
        verify(eventPublisher).publishEvent(any(CityPriorityUpdatedEvent.class));
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
        ReflectionTestUtils.setField(city, "id", cityId);
        city.updatePriority(oldPriority);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));

        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(cityId, null);

        // when
        cityCommandService.updateCityPriority(command);

        // then
        verify(cityPriorityDomainService).adjustPriorities(cityId, oldPriority, null, 1L);
        verify(eventPublisher).publishEvent(any(CityPriorityUpdatedEvent.class));
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
        ReflectionTestUtils.setField(city, "id", cityId);
        city.updatePriority(oldPriority);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));

        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(cityId, newPriority);

        // when
        cityCommandService.updateCityPriority(command);

        // then
        verify(cityPriorityDomainService).adjustPriorities(cityId, oldPriority, newPriority, 1L);
        verify(eventPublisher).publishEvent(any(CityPriorityUpdatedEvent.class));
        assertThat(city.getPriority()).isEqualTo(newPriority);
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
        ReflectionTestUtils.setField(city, "id", cityId);

        given(cityRepository.findByIdWithLock(cityId)).willReturn(Optional.of(city));

        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(cityId, newPriority);

        // when
        cityCommandService.updateCityPriority(command);

        // then
        verify(cityPriorityDomainService).adjustPriorities(cityId, null, newPriority, 1L);
        verify(eventPublisher).publishEvent(any(CityPriorityUpdatedEvent.class));
        assertThat(city.getPriority()).isEqualTo(newPriority);
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

        verify(cityPriorityDomainService, never()).adjustPriorities(any(), any(), any(), any());
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
        verify(eventPublisher).publishEvent(any(CityCreatedEvent.class));
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
        verify(eventPublisher).publishEvent(any(CityDeletedEvent.class));
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
