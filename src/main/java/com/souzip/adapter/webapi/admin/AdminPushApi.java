package com.souzip.adapter.webapi.admin;

import com.souzip.adapter.webapi.admin.dto.PushBroadcastRequest;
import com.souzip.application.notification.FcmNotificationService;
import com.souzip.application.notification.dto.PushBroadcastResult;
import com.souzip.domain.admin.infrastructure.security.annotation.AdminAccess;
import com.souzip.global.common.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/admin/push")
@RequiredArgsConstructor
@RestController
public class AdminPushApi {

    private final FcmNotificationService fcmNotificationService;

    // 관리자가 입력한 제목·본문으로 활성 FCM 토큰 전체에 푸시를 발송합니다.
    @AdminAccess
    @PostMapping("/broadcast")
    public SuccessResponse<PushBroadcastResult> broadcast(@Valid @RequestBody PushBroadcastRequest request) {
        PushBroadcastResult result = fcmNotificationService.broadcastToAllActiveTokens(
                request.title(),
                request.body()
        );
        String message = buildMessage(result);
        return SuccessResponse.of(result, message);
    }

    private static String buildMessage(PushBroadcastResult result) {
        if (result.totalTargets() == 0) {
            return "등록된 활성 기기가 없어 전송하지 않았습니다.";
        }
        if (!result.firebaseConfigured()) {
            return "Firebase가 설정되지 않아 전송하지 않았습니다. (대상 기기 " + result.totalTargets() + "건)";
        }
        return String.format(
                "푸시 발송 완료. 대상 %d건, 성공 %d건, 실패 %d건",
                result.totalTargets(),
                result.successCount(),
                result.failCount()
        );
    }
}
