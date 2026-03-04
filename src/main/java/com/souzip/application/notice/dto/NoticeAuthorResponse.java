package com.souzip.application.notice.dto;

import java.util.UUID;

public record NoticeAuthorResponse(
        UUID authorId,
        String username
) {
    public static NoticeAuthorResponse of(UUID authorId, String username) {
        return new NoticeAuthorResponse(authorId, username);
    }
}
