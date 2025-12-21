package com.souzip.api.global.common.dto.pagination;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginationResponse<T> {

    private final List<T> content;
    private final PageInfo pagination;

    @Builder(access = AccessLevel.PRIVATE)
    protected PaginationResponse(List<T> content, PageInfo pagination) {
        this.content = content;
        this.pagination = pagination;
    }

    public static <T, R> PaginationResponse<R> of(Page<T> page, List<R> content) {
        return PaginationResponse.<R>builder()
            .content(content)
            .pagination(PageInfo.of(page))
            .build();
    }

    @Getter
    public static class PageInfo {

        private final int currentPage;
        private final int totalPages;
        private final long totalItems;
        private final int pageSize;
        private final boolean first;
        private final boolean last;
        private final boolean hasNext;
        private final boolean hasPrevious;

        @Builder(access = AccessLevel.PRIVATE)
        private PageInfo(int currentPage, int totalPages,
                         long totalItems, int pageSize,
                         boolean first, boolean last,
                         boolean hasNext, boolean hasPrevious
        ) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
            this.pageSize = pageSize;
            this.first = first;
            this.last = last;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        public static PageInfo of(Page<?> page) {
            return PageInfo.builder()
                .currentPage(toDisplayPageNumber(page.getNumber()))
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
        }

        private static int toDisplayPageNumber(int zeroBasedPageNumber) {
            return zeroBasedPageNumber + 1;
        }
    }
}
