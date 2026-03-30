package com.souzip.adapter.webapi.admin;

import com.souzip.adapter.webapi.admin.dto.EmailBroadcastRequest;
import com.souzip.adapter.webapi.admin.dto.EmailTestSendRequest;
import com.souzip.application.email.EmailBroadcastService;
import com.souzip.application.email.dto.EmailBroadcastResult;
import com.souzip.domain.admin.infrastructure.security.annotation.AdminAccess;
import com.souzip.global.common.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/admin/email")
@RequiredArgsConstructor
@RestController
public class AdminEmailApi {

    private final EmailBroadcastService emailBroadcastService;

    // 한 주소로만 테스트 발송합니다(실회원 DB 조회 없음).
    @AdminAccess
    @PostMapping("/test-send")
    public SuccessResponse<EmailBroadcastResult> testSend(@Valid @RequestBody EmailTestSendRequest request) {
        EmailBroadcastResult result = emailBroadcastService.sendTestToSingleAddress(
                request.to(),
                request.subject(),
                request.body()
        );
        return SuccessResponse.of(result, buildTestMessage(result));
    }

    // 전체 회원(이메일 보유·탈퇴 제외) 고유 주소로 동일 메일을 발송합니다.
    @AdminAccess
    @PostMapping("/broadcast")
    public SuccessResponse<EmailBroadcastResult> broadcast(@Valid @RequestBody EmailBroadcastRequest request) {
        EmailBroadcastResult result = emailBroadcastService.broadcastToAllMemberEmails(
                request.subject(),
                request.body()
        );
        return SuccessResponse.of(result, buildMessage(result));
    }

    private static String buildTestMessage(EmailBroadcastResult result) {
        if (!result.smtpConfigured()) {
            return "SMTP가 설정되지 않아 테스트 메일을 보내지 않았습니다.";
        }
        if (result.successCount() > 0) {
            return "테스트 메일을 발송했습니다.";
        }
        return "테스트 메일 발송에 실패했습니다.";
    }

    private static String buildMessage(EmailBroadcastResult result) {
        if (result.totalTargets() == 0) {
            return "발송 대상 이메일이 없어 전송하지 않았습니다.";
        }
        if (!result.smtpConfigured()) {
            return "SMTP가 설정되지 않아 전송하지 않았습니다. (대상 " + result.totalTargets() + "건)";
        }
        return String.format(
                "이메일 발송 완료. 대상 %d건, 성공 %d건, 실패 %d건",
                result.totalTargets(),
                result.successCount(),
                result.failCount()
        );
    }
}
