package com.souzip.application.notice.dto;

import com.souzip.application.file.dto.FileResponse;
import com.souzip.domain.notice.Notice;
import com.souzip.domain.notice.NoticeStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record NoticeResponse(
        Long id,
        String title,
        String content,
        UUID authorId,
        NoticeStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<FileResponse> files
) {

    public static NoticeResponse from(Notice notice, List<FileResponse> files) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getAuthorId(),
                notice.getStatus(),
                notice.getCreatedAt(),
                notice.getUpdatedAt(),
                files
        );
    }

    public static List<NoticeResponse> fromList(List<Notice> notices) {
        return notices.stream()
                .map(notice -> from(notice, List.of()))
                .toList();
    }
}
