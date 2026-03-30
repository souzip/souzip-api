package com.souzip.adapter.webapi.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailTestSendRequest(
        @NotBlank(message = "수신 이메일은 필수입니다.")
        @Email(message = "수신 이메일 형식이 올바르지 않습니다.")
        @Size(max = 320, message = "수신 이메일이 너무 깁니다.")
        String to,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
        String subject,

        @NotBlank(message = "본문은 필수입니다.")
        @Size(max = 20000, message = "본문은 20000자를 초과할 수 없습니다.")
        String body
) {
    public EmailTestSendRequest {
        to = to != null ? to.trim() : null;
        subject = subject != null ? subject.trim() : null;
        body = body != null ? body.trim() : null;
    }
}
