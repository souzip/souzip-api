package com.souzip.api.domain.search.service;

import com.souzip.api.domain.search.document.LocationDocument;
import com.souzip.api.domain.search.dto.SearchResponse;
import com.souzip.api.domain.search.repository.LocationRepository;
import com.souzip.api.global.common.dto.pagination.PaginationRequest;
import com.souzip.api.global.common.dto.pagination.PaginationResponse;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SearchService {

    private static final int MAX_CITIES_PER_COUNTRY = 1000;
    private static final boolean ENABLE_AUTO_SPLIT = false;
    private static final int MIN_KEYWORD_LENGTH_FOR_SPLIT = 6;

    private final LocationRepository locationRepository;
    private static final ThreadLocal<String> CURRENT_KEYWORD = new ThreadLocal<>();

    public PaginationResponse<SearchResponse> search(String keyword, PaginationRequest paginationRequest) {
        validateAndPrepareSearch(keyword);

        String trimmedKeyword = keyword.trim();
        String[] keywords = splitKeywords(trimmedKeyword);

        setCurrentKeyword(keywords, trimmedKeyword);

        try {
            return executeSearch(keywords, trimmedKeyword, paginationRequest);
        } finally {
            CURRENT_KEYWORD.remove();
        }
    }

    private void validateAndPrepareSearch(String keyword) {
        validateKeyword(keyword);
        ensureIndexReady();
    }

    private String[] splitKeywords(String keyword) {
        String[] spaceSplit = keyword.split("\\s+");
        if (spaceSplit.length > 1) {
            return spaceSplit;
        }

        if (ENABLE_AUTO_SPLIT && keyword.length() >= MIN_KEYWORD_LENGTH_FOR_SPLIT) {
            String[] autoSplit = tryAutoSplit(keyword);
            if (autoSplit.length > 1) {
                log.debug("키워드 자동 분리: {} -> {}", keyword, Arrays.toString(autoSplit));
                return autoSplit;
            }
        }

        return new String[]{keyword};
    }

    private String[] tryAutoSplit(String keyword) {
        List<String[]> possibleSplits = generatePossibleSplits(keyword);

        for (String[] split : possibleSplits) {
            if (isValidSplit(split)) {
                return split;
            }
        }

        return new String[]{keyword};
    }

    private List<String[]> generatePossibleSplits(String keyword) {
        List<String[]> splits = new ArrayList<>();

        for (int i = 2; i <= keyword.length() - 2; i++) {
            String first = keyword.substring(0, i);
            String second = keyword.substring(i);
            splits.add(new String[]{first, second});
        }

        return splits;
    }

    private boolean isValidSplit(String[] keywords) {
        if (Arrays.stream(keywords).anyMatch(k -> k.length() < 2)) {
            return false;
        }

        for (String keyword : keywords) {
            try {
                List<SearchHit<LocationDocument>> results =
                    locationRepository.searchByKeywordWithFuzzy(keyword);

                if (results.isEmpty()) {
                    return false;
                }
            } catch (Exception e) {
                log.debug("키워드 검증 중 오류: {}", keyword, e);
                return false;
            }
        }

        return true;
    }

    private void setCurrentKeyword(String[] keywords, String trimmedKeyword) {
        if (isSingleKeyword(keywords)) {
            CURRENT_KEYWORD.set(trimmedKeyword);
            return;
        }
        CURRENT_KEYWORD.set(keywords[0]);
    }

    private PaginationResponse<SearchResponse> executeSearch(
        String[] keywords,
        String trimmedKeyword,
        PaginationRequest paginationRequest
    ) {
        Map<String, SearchHit<LocationDocument>> allHitsMap = searchByKeywordCount(keywords, trimmedKeyword);
        List<SearchResponse> allLocations = convertToResponsesWithScore(allHitsMap.values());
        List<SearchResponse> sortedLocations = sortByPriority(allLocations);
        return paginateResults(sortedLocations, paginationRequest);
    }

    private List<SearchResponse> sortByPriority(List<SearchResponse> responses) {
        return responses.stream()
            .sorted(this::compareByPriority)
            .toList();
    }

    private int compareByPriority(SearchResponse a, SearchResponse b) {
        if (hasBothPriority(a, b)) {
            return comparePriority(a, b);
        }
        if (hasNoPriority(a, b)) {
            return compareScore(a, b);
        }
        return priorityFirst(a);
    }

    private boolean hasBothPriority(SearchResponse a, SearchResponse b) {
        return a.priority() != null && b.priority() != null;
    }

    private boolean hasNoPriority(SearchResponse a, SearchResponse b) {
        return a.priority() == null && b.priority() == null;
    }

    private int comparePriority(SearchResponse a, SearchResponse b) {
        return Integer.compare(a.priority(), b.priority());
    }

    private int compareScore(SearchResponse a, SearchResponse b) {
        float scoreA = getScore(a);
        float scoreB = getScore(b);
        return Float.compare(scoreB, scoreA);
    }

    private float getScore(SearchResponse response) {
        if (response.score() == null) {
            return 0f;
        }
        return response.score();
    }

    private int priorityFirst(SearchResponse a) {
        if (a.priority() != null) {
            return -1;
        }
        return 1;
    }

    private Map<String, SearchHit<LocationDocument>> searchByKeywordCount(String[] keywords, String trimmedKeyword) {
        if (isSingleKeyword(keywords)) {
            return searchSingleKeyword(trimmedKeyword);
        }
        return searchMultipleKeywords(keywords);
    }

    private Map<String, SearchHit<LocationDocument>> searchSingleKeyword(String keyword) {
        List<SearchHit<LocationDocument>> allResults = locationRepository.searchByKeywordWithFuzzy(keyword);
        Map<String, SearchHit<LocationDocument>> finalResults = convertToMap(allResults);

        addCountryCitiesIfNeeded(allResults, finalResults);

        return finalResults;
    }

    private Map<String, SearchHit<LocationDocument>> convertToMap(List<SearchHit<LocationDocument>> hits) {
        Map<String, SearchHit<LocationDocument>> map = new LinkedHashMap<>();
        hits.forEach(hit -> map.put(hit.getContent().getId(), hit));
        return map;
    }

    private void addCountryCitiesIfNeeded(
        List<SearchHit<LocationDocument>> searchHits,
        Map<String, SearchHit<LocationDocument>> results
    ) {
        List<LocationDocument> countries = extractCountries(searchHits);
        if (!hasCountries(countries)) {
            return;
        }
        addAllCitiesFromCountries(countries, results);
    }

    private void addAllCitiesFromCountries(
        List<LocationDocument> countries,
        Map<String, SearchHit<LocationDocument>> results
    ) {
        countries.forEach(country -> addCitiesForCountry(country, results));
    }

    private void addCitiesForCountry(
        LocationDocument country,
        Map<String, SearchHit<LocationDocument>> results
    ) {
        List<LocationDocument> cities = fetchCitiesForCountry(country);
        addCitiesToResults(cities, results, 1500.0f);
    }

    private List<LocationDocument> fetchCitiesForCountry(LocationDocument country) {
        return locationRepository.findByCountryNameKrOrCountryNameEn(
            country.getNameKr(),
            country.getNameEn(),
            PageRequest.of(0, MAX_CITIES_PER_COUNTRY)
        );
    }

    private void addCitiesToResults(
        List<LocationDocument> cities,
        Map<String, SearchHit<LocationDocument>> results,
        float score
    ) {
        cities.forEach(city -> addCityIfNotExists(city, results, score));
    }

    private void addCityIfNotExists(
        LocationDocument city,
        Map<String, SearchHit<LocationDocument>> results,
        float score
    ) {
        String id = city.getId();
        if (isNotInResults(id, results)) {
            results.put(id, createSearchHit(city, score));
        }
    }

    private boolean isNotInResults(String id, Map<String, SearchHit<LocationDocument>> results) {
        return !results.containsKey(id);
    }

    private Map<String, SearchHit<LocationDocument>> searchMultipleKeywords(String[] keywords) {
        List<SearchHit<LocationDocument>> hits = fetchMultipleKeywordHits(keywords);
        return convertToMap(hits);
    }

    private List<SearchHit<LocationDocument>> fetchMultipleKeywordHits(String[] keywords) {
        return locationRepository.searchWithMultipleKeywords(keywords)
            .stream()
            .filter(hit -> isCity(hit.getContent()))
            .toList();
    }

    private SearchHit<LocationDocument> createSearchHit(LocationDocument document, Float score) {
        return new SearchHit<>(
            null, document.getId(), null, score, null,
            Map.of(), null, null, null, null, document
        );
    }

    private PaginationResponse<SearchResponse> paginateResults(
        List<SearchResponse> allLocations,
        PaginationRequest paginationRequest
    ) {
        Pageable pageable = paginationRequest.toPageable();
        List<SearchResponse> paginatedLocations = extractPageContent(allLocations, pageable);
        Page<SearchResponse> page = createPage(paginatedLocations, pageable, allLocations.size());
        return PaginationResponse.of(page, paginatedLocations);
    }

    private List<SearchResponse> extractPageContent(List<SearchResponse> allLocations, Pageable pageable) {
        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), allLocations.size());
        return getPaginatedContent(allLocations, startIndex, endIndex);
    }

    private List<SearchResponse> getPaginatedContent(List<SearchResponse> allLocations, int start, int end) {
        if (isOutOfBounds(start, allLocations.size())) {
            return List.of();
        }
        return allLocations.subList(start, end);
    }

    private boolean isOutOfBounds(int index, int size) {
        return index >= size;
    }

    private Page<SearchResponse> createPage(List<SearchResponse> content, Pageable pageable, int total) {
        return new PageImpl<>(content, pageable, total);
    }

    private void validateKeyword(String keyword) {
        if (isInvalidKeyword(keyword)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "검색어를 입력해주세요.");
        }
    }

    private boolean isInvalidKeyword(String keyword) {
        return keyword == null || keyword.isBlank();
    }

    private void ensureIndexReady() {
        if (canAccessIndex()) {
            return;
        }
        retryAfterDelay();
    }

    private boolean canAccessIndex() {
        try {
            locationRepository.count();
            return true;
        } catch (Exception e) {
            log.warn("인덱스 준비 대기 중");
            return false;
        }
    }

    private void retryAfterDelay() {
        sleepOneSecond();
        verifyIndexReady();
    }

    private void verifyIndexReady() {
        try {
            locationRepository.count();
        } catch (Exception e) {
            throw createIndexNotReadyException(e);
        }
    }

    private BusinessException createIndexNotReadyException(Exception e) {
        log.error("인덱스가 준비되지 않았습니다.", e);
        return new BusinessException(ErrorCode.SEARCH_INDEX_NOT_READY);
    }

    private void sleepOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            handleInterruption(e);
        }
    }

    private void handleInterruption(InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new BusinessException(ErrorCode.SEARCH_SERVICE_ERROR, "검색 중 중단되었습니다.");
    }

    private boolean isSingleKeyword(String[] keywords) {
        return keywords.length == 1;
    }

    private List<SearchResponse> convertToResponsesWithScore(Collection<SearchHit<LocationDocument>> searchHits) {
        return searchHits.stream()
            .map(this::convertToResponse)
            .toList();
    }

    private SearchResponse convertToResponse(SearchHit<LocationDocument> hit) {
        LocationDocument doc = hit.getContent();
        String keyword = CURRENT_KEYWORD.get();
        Map<String, List<String>> highlight = buildCustomHighlight(doc, keyword);
        Float score = hit.getScore();

        return SearchResponse.from(doc, score, highlight);
    }

    private Map<String, List<String>> buildCustomHighlight(LocationDocument doc, String keyword) {
        if (isInvalidKeyword(keyword)) {
            log.warn("키워드가 유효하지 않아 하이라이트를 생성할 수 없습니다.");
            return Map.of();
        }
        return createHighlightMap(doc, keyword);
    }

    private Map<String, List<String>> createHighlightMap(LocationDocument doc, String keyword) {
        Map<String, List<String>> highlight = new HashMap<>();
        addHighlightIfMatched(highlight, "nameKr", doc.getNameKr(), keyword);
        addHighlightIfMatched(highlight, "nameEn", doc.getNameEn(), keyword);
        addHighlightIfMatched(highlight, "countryNameKr", doc.getCountryNameKr(), keyword);
        addHighlightIfMatched(highlight, "countryNameEn", doc.getCountryNameEn(), keyword);
        return highlight;
    }

    private void addHighlightIfMatched(Map<String, List<String>> highlight, String field, String text, String keyword) {
        String highlighted = highlightText(text, keyword);
        if (!hasHighlight(highlighted)) {
            return;
        }
        addHighlightToMap(highlight, field, highlighted);
    }

    private void addHighlightToMap(Map<String, List<String>> highlight, String field, String highlighted) {
        highlight.put(field, List.of(highlighted));
    }

    private boolean hasHighlight(String highlighted) {
        return highlighted != null;
    }

    private String highlightText(String text, String keyword) {
        if (cannotHighlight(text, keyword)) {
            return null;
        }
        return createHighlightedText(text, keyword);
    }

    private boolean cannotHighlight(String text, String keyword) {
        return text == null || keyword == null;
    }

    private String createHighlightedText(String text, String keyword) {
        int index = findKeywordIndex(text, keyword);
        if (isNotFound(index)) {
            return null;
        }
        return buildHighlightedString(text, keyword, index);
    }

    private int findKeywordIndex(String text, String keyword) {
        return text.toLowerCase().indexOf(keyword.toLowerCase());
    }

    private boolean isNotFound(int index) {
        return index == -1;
    }

    private String buildHighlightedString(String text, String keyword, int index) {
        String before = text.substring(0, index);
        String matched = text.substring(index, index + keyword.length());
        String after = text.substring(index + keyword.length());
        return before + "<em>" + matched + "</em>" + after;
    }

    private List<LocationDocument> extractCountries(List<SearchHit<LocationDocument>> searchHits) {
        return searchHits.stream()
            .map(SearchHit::getContent)
            .filter(this::isCountry)
            .toList();
    }

    private boolean isCountry(LocationDocument document) {
        return "country".equals(document.getType());
    }

    private boolean hasCountries(List<LocationDocument> countries) {
        return !countries.isEmpty();
    }

    private boolean isCity(LocationDocument document) {
        return "city".equals(document.getType());
    }
}
