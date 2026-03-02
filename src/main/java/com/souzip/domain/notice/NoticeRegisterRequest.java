package com.souzip.domain.notice;

import java.util.UUID;

public record NoticeRegisterRequest(
        String title,
        String content,
        UUID authorId,
        NoticeStatus status
) {
    public static NoticeRegisterRequest of(
            String title,
            String content,
            UUID authorId,
            NoticeStatus status
    ) {
        return new NoticeRegisterRequest(title, content, authorId, status);
    }
}
