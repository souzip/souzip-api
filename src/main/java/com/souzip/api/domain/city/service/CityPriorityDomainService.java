package com.souzip.api.domain.city.service;

import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.repository.CityRepository;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CityPriorityDomainService {

    private final CityRepository cityRepository;

    public void adjustPriorities(Integer oldPriority, Integer newPriority, Long countryId) {
        pullOldPriorityIfExists(oldPriority, countryId);
        pushNewPriorityIfExists(newPriority, countryId);
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
