package com.souzip.adapter.webapi.admin.dto;

import com.souzip.domain.notice.NoticeRegisterRequest;
import com.souzip.domain.notice.NoticeStatus;
import com.souzip.domain.notice.NoticeUpdateRequest;

import java.util.UUID;

public record NoticeRequest(
        String title,
        String content,
        NoticeStatus status
) {
    public NoticeRegisterRequest toDomain(UUID authorId) {
        return NoticeRegisterRequest.of(title, content, authorId, status);
    }

    public NoticeUpdateRequest toDomain() {
        return NoticeUpdateRequest.of(title, content, status);
    }
}
