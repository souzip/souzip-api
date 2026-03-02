package com.souzip.adapter.webapi.admin.dto;

import com.souzip.domain.notice.NoticeRegisterRequest;
import com.souzip.domain.notice.NoticeStatus;
import com.souzip.domain.notice.NoticeUpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record NoticeRequest(

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 10000, message = "내용은 10000자를 초과할 수 없습니다.")
        String content,

        @NotNull(message = "상태는 필수입니다.")
        NoticeStatus status
) {
    public NoticeRegisterRequest toDomain(UUID adminId) {
        return NoticeRegisterRequest.of(
                title.trim(),
                content.trim(),
                adminId,
                status
        );
    }

    public NoticeUpdateRequest toDomain() {
        return NoticeUpdateRequest.of(
                title.trim(),
                content.trim(),
                status
        );
    }
}
