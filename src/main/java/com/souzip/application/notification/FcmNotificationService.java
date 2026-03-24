package com.souzip.application.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.souzip.application.notification.dto.PushBroadcastResult;
import com.souzip.application.notification.required.FcmTokenRepository;
import com.souzip.domain.notification.FcmToken;
import com.souzip.global.exception.BusinessException;
import com.souzip.global.exception.ErrorCode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmNotificationService {

    private final FcmTokenRepository fcmTokenRepository;
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
            throw new BusinessException(ErrorCode.FCM_SEND_FAILED, e.getMessage());
        }
    }

    // 사용자의 활성 토큰 전부에 동일 알림을 보냅니다.
    public void sendToUser(Long userId, String title, String body) {
        List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndActiveTrue(userId);
        if (tokens.isEmpty()) {
            return;
        }
        int successCount = 0;
        int failCount = 0;
        for (FcmToken token : tokens) {
            try {
                sendToToken(token.getToken(), title, body);
                successCount++;
            } catch (BusinessException e) {
                failCount++;
                log.warn(
                        "FCM 전송 실패(다음 토큰으로 계속) userId={}, fcmTokenId={}, errorCode={}",
                        userId,
                        token.getId(),
                        e.getErrorCode()
                );
            }
        }
        if (failCount > 0 && successCount == 0) {
            throw new BusinessException(ErrorCode.FCM_SEND_FAILED, "활성 토큰 전송이 모두 실패했습니다.");
        }
        if (failCount > 0) {
            log.warn("FCM 일부 토큰 실패 userId={}, 성공={}, 실패={}", userId, successCount, failCount);
        }
    }

    // 활성 FCM 토큰이 등록된 모든 기기로 동일 알림을 보냅니다.
    public PushBroadcastResult broadcastToAllActiveTokens(String title, String body) {
        List<FcmToken> tokens = fcmTokenRepository.findAllByActiveTrue();
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
        for (FcmToken token : tokens) {
            try {
                sendToToken(token.getToken(), title, body);
                successCount++;
            } catch (BusinessException e) {
                failCount++;
                log.warn(
                        "FCM 브로드캐스트 실패(다음 토큰으로 계속) fcmTokenId={}, errorCode={}",
                        token.getId(),
                        e.getErrorCode()
                );
            }
        }
        return new PushBroadcastResult(tokens.size(), successCount, failCount, true);
    }

    private static String maskToken(String token) {
        if (token == null || token.length() < 12) {
            return "***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
