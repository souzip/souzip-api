package com.souzip.application.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.souzip.application.notification.dto.PushBroadcastResult;
import com.souzip.application.notification.provided.FcmTokenFinder;
import com.souzip.domain.notification.FcmToken;
import com.souzip.shared.exception.BusinessException;
import com.souzip.shared.exception.ErrorCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmNotificationService {

    private final FcmTokenFinder fcmTokenFinder;
    private final FcmTokenCommandService fcmTokenCommandService;
    private final ObjectProvider<FirebaseMessaging> firebaseMessaging;

    // 단일 기기 토큰으로 알림(제목·본문)을 전송합니다.
    public void sendToToken(String registrationToken, String title, String body) {
        sendToToken(registrationToken, title, body, Map.of());
    }

    // 데이터 페이로드를 포함해 전송합니다.
    public void sendToToken(String registrationToken, String title, String body, Map<String, String> data) {
        FirebaseMessaging messaging = firebaseMessaging.getIfAvailable();
        if (messaging == null) {
            log.warn("FirebaseMessaging 빈이 없습니다. firebase.enabled 와 credentials 를 확인하세요. 전송을 건너뜁니다.");
            return;
        }
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();
            Message.Builder messageBuilder = Message.builder()
                    .setToken(registrationToken)
                    .setNotification(notification);
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(new HashMap<>(data));
            }
            messaging.send(messageBuilder.build());
        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패 tokenPrefix={} error={}", maskToken(registrationToken), e.getMessagingErrorCode(), e);
            // 예외 메시지에 원본 토큰 등 민감 정보가 섞일 수 있어 그대로 전파하지 않고, 오류 코드만 보존합니다.
            throw new FcmSendException(e.getMessagingErrorCode());
        }
    }

    // DB 조회는 FcmTokenFinder(짧은 readOnly 트랜잭션)에서만 하고, FCM 전송은 트랜잭션 밖에서 수행합니다.
    public void sendToUser(Long userId, String title, String body) {
        List<FcmToken> tokens = fcmTokenFinder.getActiveTokensByUserId(userId);
        if (tokens.isEmpty()) {
            return;
        }
        int successCount = 0;
        int failCount = 0;
        List<Long> staleTokenIds = new ArrayList<>();
        for (FcmToken token : tokens) {
            try {
                sendToToken(token.getToken(), title, body);
                successCount++;
            } catch (FcmSendException e) {
                failCount++;
                if (e.isPermanentFailure()) {
                    staleTokenIds.add(token.getId());
                }
                log.warn(
                        "FCM 전송 실패(다음 토큰으로 계속) userId={}, fcmTokenId={}, errorCode={}",
                        userId,
                        token.getId(),
                        e.getMessagingErrorCode()
                );
            }
        }
        deactivateStaleTokens(staleTokenIds);
        if (failCount > 0 && successCount == 0) {
            throw new BusinessException(ErrorCode.FCM_SEND_FAILED, "활성 토큰 전송이 모두 실패했습니다.");
        }
        if (failCount > 0) {
            log.warn("FCM 일부 토큰 실패 userId={}, 성공={}, 실패={}", userId, successCount, failCount);
        }
    }

    // DB 조회는 FcmTokenFinder(짧은 readOnly 트랜잭션)에서만 하고, FCM 전송 루프는 트랜잭션 밖에서 수행합니다.
    public PushBroadcastResult broadcastToAllActiveTokens(String title, String body) {
        List<FcmToken> tokens = fcmTokenFinder.getAllActiveTokens();
        if (tokens.isEmpty()) {
            return new PushBroadcastResult(0, 0, 0, true);
        }
        FirebaseMessaging messaging = firebaseMessaging.getIfAvailable();
        if (messaging == null) {
            log.warn("FirebaseMessaging 빈이 없습니다. 브로드캐스트를 건너뜁니다. 대상 기기 수={}", tokens.size());
            return new PushBroadcastResult(tokens.size(), 0, 0, false);
        }
        int successCount = 0;
        int failCount = 0;
        List<Long> staleTokenIds = new ArrayList<>();
        for (FcmToken token : tokens) {
            try {
                sendToToken(token.getToken(), title, body);
                successCount++;
            } catch (FcmSendException e) {
                failCount++;
                if (e.isPermanentFailure()) {
                    staleTokenIds.add(token.getId());
                }
                log.warn(
                        "FCM 브로드캐스트 실패(다음 토큰으로 계속) fcmTokenId={}, errorCode={}",
                        token.getId(),
                        e.getMessagingErrorCode()
                );
            }
        }
        deactivateStaleTokens(staleTokenIds);
        return new PushBroadcastResult(tokens.size(), successCount, failCount, true);
    }

    // 영구 실패(UNREGISTERED 등) 토큰은 이후 전송 대상에서 제외되도록 비활성화합니다.
    private void deactivateStaleTokens(List<Long> staleTokenIds) {
        if (staleTokenIds.isEmpty()) {
            return;
        }
        fcmTokenCommandService.deactivateByIds(staleTokenIds);
        log.info("영구 실패로 비활성화한 FCM 토큰 수={}", staleTokenIds.size());
    }

    private static String maskToken(String token) {
        if (token == null || token.length() < 12) {
            return "***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
