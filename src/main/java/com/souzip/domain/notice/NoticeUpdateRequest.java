package com.souzip.domain.notice;

public record NoticeUpdateRequest(
        String title,
        String content,
        NoticeStatus status
) {
    public static NoticeUpdateRequest of(String title, String content, NoticeStatus status) {
        return new NoticeUpdateRequest(title, content, status);
    }
}
