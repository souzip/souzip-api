package com.souzip.api.domain.city.application.command;

import com.souzip.api.domain.city.application.port.CityManagementPort;
import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.repository.CityRepository;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.repository.CountryRepository;
import com.souzip.api.domain.search.scheduler.SearchIndexScheduler;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CityCommandService implements CityManagementPort {

    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final SearchIndexScheduler searchIndexScheduler;

    @Transactional
    @Override
    public void updateCityPriority(UpdateCityPriorityCommand command) {
        City city = findCityByIdWithLock(command.cityId());
        Integer oldPriority = city.getPriority();
        Long countryId = city.getCountry().getId();

        adjustPriorities(oldPriority, command.newPriority(), countryId);
        city.updatePriority(command.newPriority());
        searchIndexScheduler.markReindexNeeded();
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
        searchIndexScheduler.markReindexNeeded();
    }

    @Transactional
    @Override
    public void deleteCity(DeleteCityCommand command) {
        City city = findCityById(command.cityId());
        cityRepository.delete(city);
        searchIndexScheduler.markReindexNeeded();
    }

    private void adjustPriorities(Integer oldPriority, Integer newPriority, Long countryId) {
        pullOldPriorityIfExists(oldPriority, countryId);
        pushNewPriorityIfExists(newPriority, countryId);
    }

    private void pullOldPriorityIfExists(Integer oldPriority, Long countryId) {
        if (hasPriority(oldPriority)) {
            AtomicInteger expected = new AtomicInteger(oldPriority + 1);

            cityRepository
                .findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, oldPriority + 1)
                .stream()
                .takeWhile(city -> city.getPriority().equals(expected.get()))
                .forEach(city -> city.updatePriority(expected.getAndIncrement() - 1));
        }
    }

    private void pushNewPriorityIfExists(Integer newPriority, Long countryId) {
        if (hasPriority(newPriority)) {
            AtomicInteger expected = new AtomicInteger(newPriority);

            cityRepository
                .findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, newPriority)
                .stream()
                .takeWhile(city -> city.getPriority().equals(expected.get()))
                .forEach(city -> city.updatePriority(expected.getAndIncrement() + 1));
        }
    }

    private boolean hasPriority(Integer priority) {
        return priority != null;
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
