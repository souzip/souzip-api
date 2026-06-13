package com.souzip.application.notification;

import com.google.firebase.messaging.MessagingErrorCode;
import com.souzip.shared.exception.BusinessException;
import com.souzip.shared.exception.ErrorCode;

// FCM 전송 실패를 나타내며, 영구 실패 여부 판별을 위해 MessagingErrorCode 를 보존합니다.
// (예외 메시지에는 토큰 등 민감 정보를 담지 않습니다.)
public class FcmSendException extends BusinessException {

    private final transient MessagingErrorCode messagingErrorCode;

    public FcmSendException(MessagingErrorCode messagingErrorCode) {
        super(ErrorCode.FCM_SEND_FAILED);
        this.messagingErrorCode = messagingErrorCode;
    }

    public MessagingErrorCode getMessagingErrorCode() {
        return messagingErrorCode;
    }

    // 토큰이 더 이상 유효하지 않아 재전송해도 계속 실패하는 영구 오류인지 여부.
    public boolean isPermanentFailure() {
        return messagingErrorCode == MessagingErrorCode.UNREGISTERED
                || messagingErrorCode == MessagingErrorCode.INVALID_ARGUMENT
                || messagingErrorCode == MessagingErrorCode.SENDER_ID_MISMATCH;
    }
}
