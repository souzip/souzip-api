package com.souzip.adapter.webapi.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PushBroadcastRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 1000, message = "내용은 1000자를 초과할 수 없습니다.")
        String body
) {
    public PushBroadcastRequest {
        title = title != null ? title.trim() : null;
        body = body != null ? body.trim() : null;
    }
}
