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

    public void adjustPriorities(Integer oldPriority, Integer newPriority, Long countryId) {
        if (isSamePriority(oldPriority, newPriority)) {
            return;
        }

        if (isSettingNewPriority(oldPriority, newPriority)) {
            pushNewPriorityIfExists(newPriority, countryId);
            return;
        }

        if (isRemovingPriority(oldPriority, newPriority)) {
            pullOldPriorityIfExists(oldPriority, countryId);
            return;
        }

        if (isMovingDown(oldPriority, newPriority)) {
            pullOldPriorityIfExists(oldPriority, countryId);
            pushNewPriorityIfExists(newPriority - 1, countryId);
            return;
        }

        if (isMovingUp(oldPriority, newPriority)) {
            pushNewPriorityIfExists(newPriority, countryId);
            pullOldPriorityIfExists(oldPriority + 1, countryId);
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

    private void pullOldPriorityIfExists(Integer oldPriority, Long countryId) {
        if (hasPriority(oldPriority)) {
            AtomicInteger expected = new AtomicInteger(oldPriority + 1);

            List<City> citiesToPull = cityRepository
                    .findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, oldPriority + 1);

            citiesToPull.stream()
                    .takeWhile(city -> city.getPriority().equals(expected.get()))
                    .forEach(city -> city.updatePriority(expected.getAndIncrement() - 1));
        }
    }

    private void pushNewPriorityIfExists(Integer newPriority, Long countryId) {
        if (hasPriority(newPriority)) {
            AtomicInteger expected = new AtomicInteger(newPriority);

            List<City> citiesToPush = cityRepository
                    .findByCountryIdAndPriorityGoeOrderByPriorityAsc(countryId, newPriority);

            citiesToPush.stream()
                    .takeWhile(city -> city.getPriority().equals(expected.get()))
                    .forEach(city -> city.updatePriority(expected.getAndIncrement() + 1));
        }
    }

    private boolean hasPriority(Integer priority) {
        return priority != null;
    }
}
