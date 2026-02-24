package com.souzip.api.domain.city.service;

import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.repository.CityRepository;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CityPriorityDomainService {

    private final CityRepository cityRepository;

    public void adjustPriorities(Long excludeCityId, Integer oldPriority, Integer newPriority, Long countryId) {
        if (isSamePriority(oldPriority, newPriority)) {
            return;
        }

        if (isSettingNewPriority(oldPriority, newPriority)) {
            pushNewPriorityIfExists(excludeCityId, newPriority, null, countryId);
            return;
        }

        if (isRemovingPriority(oldPriority, newPriority)) {
            pullOldPriorityIfExists(excludeCityId, oldPriority, null, countryId);
            return;
        }

        if (isMovingDown(oldPriority, newPriority)) {
            shiftLeftBetween(excludeCityId, countryId, oldPriority + 1, newPriority);
            return;
        }

        if (isMovingUp(oldPriority, newPriority)) {
            shiftRightBetween(excludeCityId, countryId, newPriority, oldPriority - 1);
        }
    }

    private boolean isSamePriority(Integer oldPriority, Integer newPriority) {
        return Objects.equals(oldPriority, newPriority);
    }

    private boolean isSettingNewPriority(Integer oldPriority, Integer newPriority) {
        return oldPriority == null && newPriority != null;
    }

    private boolean isRemovingPriority(Integer oldPriority, Integer newPriority) {
        return oldPriority != null && newPriority == null;
    }

    private boolean isMovingDown(Integer oldPriority, Integer newPriority) {
        return oldPriority != null && newPriority != null && oldPriority < newPriority;
    }

    private boolean isMovingUp(Integer oldPriority, Integer newPriority) {
        return oldPriority != null && newPriority != null && oldPriority > newPriority;
    }

    private void pullOldPriorityIfExists(Long excludeCityId, Integer oldPriority, Integer endPriority, Long countryId) {
        if (!hasPriority(oldPriority)) {
            return;
        }

        AtomicInteger expected = new AtomicInteger(oldPriority + 1);

        List<City> citiesToPull = cityRepository
                .findByCountryIdAndPriorityGoeOrderByPriorityAscWithLock(countryId, oldPriority + 1)
                .stream()
                .filter(city -> !city.getId().equals(excludeCityId))
                .toList();

        citiesToPull.stream()
                .takeWhile(city ->
                        city.getPriority().equals(expected.get())
                                && (endPriority == null || city.getPriority() <= endPriority)
                )
                .forEach(city -> city.updatePriority(expected.getAndIncrement() - 1));
    }

    private void pushNewPriorityIfExists(Long excludeCityId, Integer newPriority, Integer endPriority, Long countryId) {
        if (!hasPriority(newPriority)) {
            return;
        }

        AtomicInteger expected = new AtomicInteger(newPriority);

        List<City> citiesToPush = cityRepository
                .findByCountryIdAndPriorityGoeOrderByPriorityAscWithLock(countryId, newPriority)
                .stream()
                .filter(city -> !city.getId().equals(excludeCityId))
                .toList();

        citiesToPush.stream()
                .takeWhile(city ->
                        city.getPriority().equals(expected.get())
                                && (endPriority == null || city.getPriority() <= endPriority)
                )
                .forEach(city -> city.updatePriority(expected.getAndIncrement() + 1));
    }

    private void shiftLeftBetween(Long excludeCityId, Long countryId, Integer startInclusive, Integer endInclusive) {
        if (startInclusive > endInclusive) {
            return;
        }

        List<City> cities = cityRepository
                .findByCountryIdAndPriorityBetweenOrderByPriorityAscWithLock(countryId, startInclusive, endInclusive)
                .stream()
                .filter(city -> !city.getId().equals(excludeCityId))
                .toList();

        cities.forEach(city -> city.updatePriority(city.getPriority() - 1));
    }

    private void shiftRightBetween(Long excludeCityId, Long countryId, Integer startInclusive, Integer endInclusive) {
        if (startInclusive > endInclusive) {
            return;
        }

        List<City> cities = cityRepository
                .findByCountryIdAndPriorityBetweenOrderByPriorityAscWithLock(countryId, startInclusive, endInclusive)
                .stream()
                .filter(city -> !city.getId().equals(excludeCityId))
                .toList();

        cities.forEach(city -> city.updatePriority(city.getPriority() + 1));
    }

    private boolean hasPriority(Integer priority) {
        return priority != null;
    }
}
