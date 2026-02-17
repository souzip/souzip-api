package com.souzip.api.domain.search.scheduler;

import com.souzip.api.domain.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
@Component
public class SearchIndexScheduler {

    private final SearchIndexService searchIndexService;
    private final AtomicBoolean reindexNeeded = new AtomicBoolean(false);

    public void markReindexNeeded() {
        reindexNeeded.set(true);
    }

    @Scheduled(fixedDelay = 30000)
    public void reindexIfNeeded() {
        if (isReindexNotNeeded()) {
            return;
        }
        executeReindex();
    }

    private boolean isReindexNotNeeded() {
        return !reindexNeeded.get();
    }

    private void executeReindex() {
        reindexNeeded.set(false);
        log.info("우선순위 변경 감지 - reindex 시작");
        searchIndexService.reindexAll();
        log.info("reindex 완료");
    }
}
