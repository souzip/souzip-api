package com.souzip.api.domain.search.service;

import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.search.dto.SearchResponse;
import com.souzip.api.domain.search.repository.LocationSearchRepository;
import com.souzip.api.global.common.dto.pagination.PaginationRequest;
import com.souzip.api.global.common.dto.pagination.PaginationResponse;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SearchService {

    private final LocationSearchRepository locationSearchRepository;

    public PaginationResponse<SearchResponse> search(String keyword, PaginationRequest paginationRequest) {
        validateKeyword(keyword);

        String trimmedKeyword = keyword.trim();
        Pageable pageable = paginationRequest.toPageable();

        List<City> cities = locationSearchRepository.searchByKeyword(trimmedKeyword, pageable);
        long totalCount = locationSearchRepository.countByKeyword(trimmedKeyword);

        List<SearchResponse> responses = cities.stream()
                .map(this::convertToResponse)
                .toList();

        Page<SearchResponse> page = new PageImpl<>(responses, pageable, totalCount);

        return PaginationResponse.of(page, responses);
    }

    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "검색어를 입력해주세요.");
        }
    }

    private SearchResponse convertToResponse(City city) {
        return SearchResponse.of(
                city.getId(),
                "city",
                city.getNameKr(),
                city.getNameEn(),
                city.getCountry() != null ? city.getCountry().getNameKr() : null,
                city.getCountry() != null ? city.getCountry().getNameEn() : null,
                city.getLatitude(),
                city.getLongitude()
        );
    }
}
