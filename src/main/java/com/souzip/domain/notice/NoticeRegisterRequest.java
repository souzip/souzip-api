package com.souzip.domain.notice;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

public record NoticeRegisterRequest(
        String title,
        String content,
        UUID authorId,
        NoticeStatus status
) {
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_CONTENT_LENGTH = 10000;

    public NoticeRegisterRequest {
        title = validateTitle(title);
        content = validateContent(content);
        requireNonNull(authorId, "작성자는 필수입니다.");
        requireNonNull(status, "상태는 필수입니다.");
    }

    public static NoticeRegisterRequest of(
            String title,
            String content,
            UUID authorId,
            NoticeStatus status
    ) {
        return new NoticeRegisterRequest(title, content, authorId, status);
    }

    private static String validateTitle(String title) {
        requireNonNull(title, "제목은 필수입니다.");

        String trimmed = title.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다.");
        }

        if (trimmed.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("제목은 %d자를 초과할 수 없습니다.", MAX_TITLE_LENGTH)
            );
        }

        return trimmed;
    }

    private static String validateContent(String content) {
        requireNonNull(content, "내용은 필수입니다.");

        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("내용은 비어있을 수 없습니다.");
        }

        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("내용은 %d자를 초과할 수 없습니다.", MAX_CONTENT_LENGTH)
            );
        }

        return trimmed;
    }
}
