package com.souzip.api.global.common.dto.pagination;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationResponseTest {

    @DisplayName("페이지 정보 확인")
    @CsvSource({
        "0, 10, 23, 1, 3, true, false, true, false",
        "1, 10, 30, 2, 3, false, false, true, true",
        "2, 10, 23, 3, 3, false, true, false, true"
    })
    @ParameterizedTest
    void pageInfo(
        int pageNumber,
        int pageSize,
        long totalElements,
        int expectedCurrentPage,
        int expectedTotalPages,
        boolean expectedFirst,
        boolean expectedLast,
        boolean expectedHasNext,
        boolean expectedHasPrevious
    ) {
        // given
        List<String> content = List.of("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(pageNumber, pageSize), totalElements);

        // when
        PaginationResponse.PageInfo pageInfo = PaginationResponse.of(page, content).getPagination();

        // then
        assertThat(pageInfo.getCurrentPage()).isEqualTo(expectedCurrentPage);
        assertThat(pageInfo.getTotalPages()).isEqualTo(expectedTotalPages);
        assertThat(pageInfo.getTotalItems()).isEqualTo(totalElements);
        assertThat(pageInfo.getPageSize()).isEqualTo(pageSize);
        assertThat(pageInfo.isFirst()).isEqualTo(expectedFirst);
        assertThat(pageInfo.isLast()).isEqualTo(expectedLast);
        assertThat(pageInfo.isHasNext()).isEqualTo(expectedHasNext);
        assertThat(pageInfo.isHasPrevious()).isEqualTo(expectedHasPrevious);
    }

    @DisplayName("0-based를 1-based로 변환")
    @CsvSource({"0,1", "1,2", "2,3"})
    @ParameterizedTest
    void toDisplayPageNumber(int pageNumber, int expectedCurrentPage) {
        // given
        List<String> content = List.of("item");
        Page<String> page = new PageImpl<>(content, PageRequest.of(pageNumber, 10), 1);

        // when
        PaginationResponse.PageInfo pageInfo = PaginationResponse.of(page, content).getPagination();

        // then
        assertThat(pageInfo.getCurrentPage()).isEqualTo(expectedCurrentPage);
    }

    @DisplayName("빈 페이지 처리")
    @Test
    void emptyPage() {
        // given
        List<String> content = List.of();
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 0);

        // when
        PaginationResponse<String> response = PaginationResponse.of(page, content);

        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getPagination().getTotalItems()).isZero();
        assertThat(response.getPagination().getTotalPages()).isZero();
    }
}
