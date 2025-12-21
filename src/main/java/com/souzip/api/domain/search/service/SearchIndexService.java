package com.souzip.api.domain.search.service;

import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.repository.CityRepository;
import com.souzip.api.domain.country.entity.Country;
import com.souzip.api.domain.country.repository.CountryRepository;
import com.souzip.api.domain.search.document.LocationDocument;
import com.souzip.api.domain.search.repository.LocationRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SearchIndexService {

    private static final int BATCH_SIZE = 1000;
    private static final int MAX_RETRIES = 3;
    private static final int MAX_HEALTH_CHECK_RETRIES = 30;
    private static final long INDEX_DELETE_WAIT_MS = 2000;
    private static final long INDEX_CHECK_INTERVAL_MS = 1000;

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final LocationRepository locationRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Transactional
    public void reindexAll() {
        log.info("인덱싱 시작");
        recreateIndex();
        indexCountries();
        indexCities();
        log.info("재인덱싱 완료");
    }

    private void indexCountries() {
        List<Country> countries = countryRepository.findAll();
        int total = countries.size();
        log.info("국가 {}개 인덱싱 시작", total);

        processCountryBatch(countries, total);

        log.info("국가 {}개 인덱싱 완료", total);
    }

    private void processCountryBatch(List<Country> countries, int total) {
        List<LocationDocument> batch = new ArrayList<>();
        int count = 0;

        for (Country country : countries) {
            count++;
            processCountryItem(country, batch, count, total);
        }

        saveRemainingBatch(batch);
    }

    private void processCountryItem(Country country, List<LocationDocument> batch, int count, int total) {
        batch.add(LocationDocument.from(country));
        saveBatchIfFull(batch, count, total, "국가");
    }

    private void saveBatchIfFull(List<LocationDocument> batch, int count, int total, String entityType) {
        if (isBatchFull(batch)) {
            saveBatchAndClear(batch, count, total, entityType);
        }
    }

    private void saveBatchAndClear(List<LocationDocument> batch, int count, int total, String entityType) {
        saveWithRetry(batch);
        log.info("{} {}/{} 인덱싱 중", entityType, count, total);
        batch.clear();
    }

    private boolean isBatchFull(List<LocationDocument> batch) {
        return batch.size() >= BATCH_SIZE;
    }

    private void saveRemainingBatch(List<LocationDocument> batch) {
        if (hasBatchItems(batch)) {
            saveWithRetry(batch);
        }
    }

    private boolean hasBatchItems(List<LocationDocument> batch) {
        return !batch.isEmpty();
    }

    private void indexCities() {
        List<City> cities = cityRepository.findAll();
        int total = cities.size();
        log.info("도시 {}개 인덱싱 시작", total);

        processCityBatch(cities, total);

        log.info("도시 {}개 인덱싱 완료", total);
    }

    private void processCityBatch(List<City> cities, int total) {
        List<LocationDocument> batch = new ArrayList<>();
        int count = 0;

        for (City city : cities) {
            count++;
            processCityItem(city, batch, count, total);
        }

        saveRemainingBatch(batch);
    }

    private void processCityItem(City city, List<LocationDocument> batch, int count, int total) {
        batch.add(LocationDocument.from(city));
        saveBatchIfFull(batch, count, total, "도시");
    }

    private void recreateIndex() {
        try {
            deleteIndexIfExists();
            createNewIndex();
            waitForIndexReady();
            log.info("인덱스 생성 완료");
        } catch (InterruptedException e) {
            handleInterruptedException(e);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            handleIndexCreationException(e);
        }
    }

    private void handleInterruptedException(InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("인덱스 생성 중 인터럽트 발생", e);
        throw new BusinessException(ErrorCode.SEARCH_INDEX_CREATE_FAILED);
    }

    private void handleIndexCreationException(Exception e) {
        log.error("인덱스 재생성 중 오류", e);
        throw new BusinessException(ErrorCode.SEARCH_INDEX_CREATE_FAILED);
    }

    private void deleteIndexIfExists() throws InterruptedException {
        if (indexExists()) {
            deleteIndex();
        }
    }

    private boolean indexExists() {
        return elasticsearchOperations.indexOps(LocationDocument.class).exists();
    }

    private void deleteIndex() throws InterruptedException {
        try {
            log.info("기존 인덱스 삭제 중");
            elasticsearchOperations.indexOps(LocationDocument.class).delete();
            Thread.sleep(INDEX_DELETE_WAIT_MS);
        } catch (Exception e) {
            log.error("인덱스 삭제 실패", e);
            throw new BusinessException(ErrorCode.SEARCH_INDEX_DELETE_FAILED);
        }
    }

    private void createNewIndex() {
        try {
            log.info("새 인덱스 생성 중");
            elasticsearchOperations.indexOps(LocationDocument.class).create();
            elasticsearchOperations.indexOps(LocationDocument.class).putMapping();
        } catch (Exception e) {
            log.error("인덱스 생성 실패", e);
            throw new BusinessException(ErrorCode.SEARCH_INDEX_CREATE_FAILED);
        }
    }

    private void waitForIndexReady() {
        log.info("인덱스 준비 상태 확인 중");
        long startTime = System.currentTimeMillis();
        checkIndexReadyWithRetry(startTime);
    }

    private void checkIndexReadyWithRetry(long startTime) {
        int retryCount = 0;

        while (retryCount < MAX_HEALTH_CHECK_RETRIES) {
            sleep(INDEX_CHECK_INTERVAL_MS);

            if (indexExists()) {
                logIndexReadyTime(startTime);
                return;
            }

            retryCount++;
        }

        throw new BusinessException(ErrorCode.SEARCH_INDEX_NOT_READY);
    }

    private void logIndexReadyTime(long startTime) {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        log.info("인덱스 준비 완료 ({}초 소요)", elapsed);
    }

    private void saveWithRetry(List<LocationDocument> documents) {
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            boolean success = trySave(documents, retryCount);

            if (success) {
                return;
            }

            retryCount++;
        }
    }

    private boolean trySave(List<LocationDocument> documents, int retryCount) {
        try {
            locationRepository.saveAll(documents);
            return true;
        } catch (Exception e) {
            handleSaveFailure(e, retryCount);
            return false;
        }
    }

    private void handleSaveFailure(Exception e, int retryCount) {
        if (isLastRetry(retryCount)) {
            throwSaveFailedException(e);
        }

        waitBeforeRetry(e, retryCount);
    }

    private boolean isLastRetry(int retryCount) {
        return retryCount == MAX_RETRIES - 1;
    }

    private void throwSaveFailedException(Exception e) {
        log.error("최대 재시도 횟수 초과. 저장 실패", e);
        throw new BusinessException(ErrorCode.SEARCH_SAVE_FAILED);
    }

    private void waitBeforeRetry(Exception e, int retryCount) {
        long waitTime = 2000L * (retryCount + 1);
        log.warn("저장 실패, {}ms 후 재시도({}/{}): {}",
            waitTime, retryCount + 1, MAX_RETRIES, e.getMessage());
        sleep(waitTime);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            handleInterruptedException(e);
        }
    }
}
