package com.souzip.shared.common.dto.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationRequestTest {

    @Test
    @DisplayName("정상적인 페이지 요청")
    void createPaginationRequest() {
        // given & when
        PaginationRequest request = new PaginationRequest(1, 10);

        // then
        assertThat(request.getPageNo()).isEqualTo(1);
        assertThat(request.getPageSize()).isEqualTo(10);
    }

    @DisplayName("null 페이지 요청 시 기본값 적용")
    @Test
    void createWithNull() {
        // given & when
        PaginationRequest request = new PaginationRequest(null, null);

        // then
        assertThat(request.getPageNo()).isEqualTo(1);
        assertThat(request.getPageSize()).isEqualTo(10);
    }

    @DisplayName("페이지 번호가 1보다 작을 때 기본값으로 적용")
    @Test
    void validatePageNoLessThanMin() {
        // given & when
        PaginationRequest request = new PaginationRequest(0, 10);

        // then
        assertThat(request.getPageNo()).isEqualTo(1);
    }

    @DisplayName("페이지 크기가 1보다 작을 때 기본값으로 적용")
    @Test
    void validatePageSizeLessThanMin() {
        // given & when
        PaginationRequest request = new PaginationRequest(1, 0);

        // then
        assertThat(request.getPageSize()).isEqualTo(10);
    }

    @DisplayName("1-based를 0-based로")
    @Test
    void toPageable() {
        // given
        PaginationRequest request = new PaginationRequest(1, 10);

        // when
        Pageable pageable = request.toPageable();

        // then
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @DisplayName("첫 페이지는 offset 0")
    @Test
    void firstPageOffset() {
        // given
        PaginationRequest request = new PaginationRequest(1, 10);

        // when
        Pageable pageable = request.toPageable();

        // then
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getOffset()).isZero();
    }
}
