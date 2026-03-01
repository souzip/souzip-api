package com.souzip.domain.notice;

import com.souzip.domain.notice.NoticeStatus;

public record NoticeRegisterRequest(
        String title,
        String content,
        Long authorId,
        NoticeStatus status
) {
    public static NoticeRegisterRequest of(
            String title,
            String content,
            Long authorId,
            NoticeStatus status
    ) {
        return new NoticeRegisterRequest(title, content, authorId, status);
    }
}
