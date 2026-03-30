package com.souzip.adapter.webapi.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailBroadcastRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
        String subject,

        @NotBlank(message = "본문은 필수입니다.")
        @Size(max = 20000, message = "본문은 20000자를 초과할 수 없습니다.")
        String body
) {
    public EmailBroadcastRequest {
        subject = subject != null ? subject.trim() : null;
        body = body != null ? body.trim() : null;
    }
}
