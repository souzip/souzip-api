package com.souzip.api.domain.search.event;

import com.souzip.api.domain.city.event.CityCreatedEvent;
import com.souzip.api.domain.city.event.CityDeletedEvent;
import com.souzip.api.domain.city.event.CityPriorityUpdatedEvent;
import com.souzip.api.domain.search.scheduler.SearchIndexScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class SearchEventHandler {

    private final SearchIndexScheduler searchIndexScheduler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCityCreated(CityCreatedEvent event) {
        log.info("City created event received: cityId={}", event.cityId());
        searchIndexScheduler.markReindexNeeded();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCityDeleted(CityDeletedEvent event) {
        log.info("City deleted event received: cityId={}", event.cityId());
        searchIndexScheduler.markReindexNeeded();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCityPriorityUpdated(CityPriorityUpdatedEvent event) {
        log.info("City priority updated event received: cityId={}, oldPriority={}, newPriority={}",
            event.cityId(), event.oldPriority(), event.newPriority());
        searchIndexScheduler.markReindexNeeded();
    }
}
