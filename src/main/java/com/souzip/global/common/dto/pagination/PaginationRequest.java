package com.souzip.global.common.dto.pagination;

import java.util.Optional;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
public class PaginationRequest {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 30;
    private static final int MIN_PAGE_NO = 1;
    private static final int MIN_PAGE_SIZE = 1;

    private final int pageNo;
    private final int pageSize;

    public PaginationRequest(Integer pageNo, Integer pageSize) {
        this.pageNo = validatePageNo(
            Optional.ofNullable(pageNo).orElse(DEFAULT_PAGE_NO)
        );
        this.pageSize = validatePageSize(
            Optional.ofNullable(pageSize).orElse(DEFAULT_PAGE_SIZE)
        );
    }

    public Pageable toPageable() {
        return PageRequest.of(pageNo - 1, pageSize);
    }

    private int validatePageNo(int pageNo) {
        if (pageNo < MIN_PAGE_NO) {
            return DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    private int validatePageSize(int pageSize) {
        if (pageSize < MIN_PAGE_SIZE) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
