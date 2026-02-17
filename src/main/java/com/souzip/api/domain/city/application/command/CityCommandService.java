package com.souzip.api.domain.city.application.command;

import com.souzip.api.domain.city.application.command.CreateCityCommand;
import com.souzip.api.domain.city.application.command.DeleteCityCommand;
import com.souzip.api.domain.city.application.command.UpdateCityPriorityCommand;
import com.souzip.api.domain.city.application.port.CityManagementPort;
import com.souzip.api.domain.city.domain.service.CityPriorityDomainService;
import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.event.CityCreatedEvent;
import com.souzip.api.domain.city.event.CityDeletedEvent;
import com.souzip.api.domain.city.event.CityPriorityUpdatedEvent;
import com.souzip.api.domain.city.repository.CityRepository;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.repository.CountryRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CityCommandService implements CityManagementPort {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CityPriorityDomainService cityPriorityDomainService;

    @Transactional
    @Override
    public void updateCityPriority(UpdateCityPriorityCommand command) {
        City city = findCityByIdWithLock(command.cityId());
        Integer oldPriority = city.getPriority();
        Long countryId = city.getCountry().getId();

        // Domain Service 위임
        cityPriorityDomainService.adjustPriorities(oldPriority, command.newPriority(), countryId);
        city.updatePriority(command.newPriority());

        eventPublisher.publishEvent(CityPriorityUpdatedEvent.of(
            city.getId(),
            oldPriority,
            command.newPriority()
        ));
    }

    @Transactional
    @Override
    public void createCity(CreateCityCommand command) {
        Country country = findCountryById(command.countryId());
        City city = City.create(
            command.nameEn(),
            command.nameKr(),
            BigDecimal.valueOf(command.latitude()),
            BigDecimal.valueOf(command.longitude()),
            country
        );
        cityRepository.save(city);

        eventPublisher.publishEvent(CityCreatedEvent.of(
            city.getId(),
            country.getId()
        ));
    }

    @Transactional
    @Override
    public void deleteCity(DeleteCityCommand command) {
        City city = findCityById(command.cityId());
        cityRepository.delete(city);

        eventPublisher.publishEvent(CityDeletedEvent.of(city.getId()));
    }

    private City findCityByIdWithLock(Long cityId) {
        return cityRepository.findByIdWithLock(cityId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "도시를 찾을 수 없습니다."));
    }

    private City findCityById(Long cityId) {
        return cityRepository.findById(cityId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "도시를 찾을 수 없습니다."));
    }

    private Country findCountryById(Long countryId) {
        return countryRepository.findById(countryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "나라를 찾을 수 없습니다."));
    }
}
