package com.souzip.api.domain.city.application.command;

import com.souzip.api.domain.admin.event.AdminCityPriorityChangeRequestedEvent;
import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.repository.CityRepository;
import com.souzip.api.domain.search.scheduler.SearchIndexScheduler;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CityCommandService {

    private final CityRepository cityRepository;
    private final SearchIndexScheduler searchIndexScheduler;

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePriorityChangeRequested(AdminCityPriorityChangeRequestedEvent event) {
        City city = findCityByIdWithLock(event.cityId());
        Integer oldPriority = city.getPriority();
        Long countryId = city.getCountry().getId();

        adjustPriorities(oldPriority, event.newPriority(), countryId);
        city.updatePriority(event.newPriority());
        searchIndexScheduler.markReindexNeeded();
    }

    private void adjustPriorities(Integer oldPriority, Integer newPriority, Long countryId) {
        pullOldPriorityIfExists(oldPriority, countryId);
        pushNewPriorityIfExists(newPriority, countryId);
    }

    private void pullOldPriorityIfExists(Integer oldPriority, Long countryId) {
        if (hasPriority(oldPriority)) {
            cityRepository.pullPriorityFrom(oldPriority, countryId);
        }
    }

    private void pushNewPriorityIfExists(Integer newPriority, Long countryId) {
        if (hasPriority(newPriority)) {
            cityRepository.shiftPriorityFrom(newPriority, countryId);
        }
    }

    private boolean hasPriority(Integer priority) {
        return priority != null;
    }

    private City findCityByIdWithLock(Long cityId) {
        return cityRepository.findByIdWithLock(cityId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "도시를 찾을 수 없습니다."));
    }
}
