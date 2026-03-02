package com.souzip.application.notice.dto;

import com.souzip.application.file.dto.FileResponse;
import com.souzip.domain.notice.Notice;
import com.souzip.domain.notice.NoticeStatus;
import java.time.LocalDateTime;
import java.util.List;

public record NoticeResponse(
        Long id,
        String title,
        String content,
        NoticeAuthorResponse author,
        NoticeStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<FileResponse> files
) {

    public static NoticeResponse from(Notice notice, NoticeAuthorResponse author, List<FileResponse> files) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                author,
                notice.getStatus(),
                notice.getCreatedAt(),
                notice.getUpdatedAt(),
                files
        );
    }
}
