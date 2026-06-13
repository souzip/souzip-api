package com.souzip.application.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.souzip.application.notification.provided.FcmTokenFinder;
import com.souzip.application.notification.dto.PushBroadcastResult;
import com.souzip.domain.notification.DeviceType;
import com.souzip.domain.notification.FcmToken;
import com.souzip.domain.notification.FcmTokenRegisterRequest;
import com.souzip.shared.exception.BusinessException;
import com.souzip.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FcmNotificationServiceTest {

    @Mock
    private FcmTokenFinder fcmTokenFinder;

    @Mock
    private FcmTokenCommandService fcmTokenCommandService;

    @Mock
    private ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @InjectMocks
    private FcmNotificationService fcmNotificationService;

    @DisplayName("Firebase 미설정 시 전송을 건너뛴다")
    @Test
    void sendToToken_firebaseNotConfigured_skips() {
        // given
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(null);

        // when & then (예외 없이 종료되어야 함)
        fcmNotificationService.sendToToken("some-token", "제목", "본문");

        then(firebaseMessaging).shouldHaveNoInteractions();
    }

    @DisplayName("사용자에게 전송 시 활성 토큰이 없으면 전송하지 않는다")
    @Test
    void sendToUser_noActiveTokens_doesNotSend() {
        // given
        given(fcmTokenFinder.getActiveTokensByUserId(1L)).willReturn(List.of());

        // when
        fcmNotificationService.sendToUser(1L, "제목", "본문");

        // then
        then(firebaseMessagingProvider).should(never()).getIfAvailable();
    }

    @DisplayName("사용자 토큰 전부 실패 시 예외를 던진다")
    @Test
    void sendToUser_allTokensFail_throwsException() throws FirebaseMessagingException {
        // given
        FcmToken token = createToken("fcm-token-1", "device-id-1", 1L);
        FirebaseMessagingException fcmException = mockFirebaseException();

        given(fcmTokenFinder.getActiveTokensByUserId(1L)).willReturn(List.of(token));
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        given(firebaseMessaging.send(any())).willThrow(fcmException);

        // when & then
        assertThatThrownBy(() -> fcmNotificationService.sendToUser(1L, "제목", "본문"))
                .isInstanceOf(BusinessException.class);
    }

    @DisplayName("브로드캐스트 - 활성 토큰 없으면 전송 건너뛰고 결과 반환")
    @Test
    void broadcastToAllActiveTokens_noTokens_returnsEmptyResult() {
        // given
        given(fcmTokenFinder.getAllActiveTokens()).willReturn(List.of());

        // when
        PushBroadcastResult result = fcmNotificationService.broadcastToAllActiveTokens("제목", "본문");

        // then
        assertThat(result.totalTargets()).isZero();
        assertThat(result.successCount()).isZero();
        assertThat(result.firebaseConfigured()).isTrue();
    }

    @DisplayName("브로드캐스트 - Firebase 미설정 시 전송 없이 결과 반환")
    @Test
    void broadcastToAllActiveTokens_firebaseNotConfigured_returnsResultWithoutSending() {
        // given
        FcmToken token = createToken("fcm-token-1", "device-id-1", 1L);

        given(fcmTokenFinder.getAllActiveTokens()).willReturn(List.of(token));
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(null);

        // when
        PushBroadcastResult result = fcmNotificationService.broadcastToAllActiveTokens("제목", "본문");

        // then
        assertThat(result.totalTargets()).isEqualTo(1);
        assertThat(result.successCount()).isZero();
        assertThat(result.firebaseConfigured()).isFalse();
    }

    @DisplayName("브로드캐스트 - 일부 토큰 실패해도 나머지는 전송된다")
    @Test
    void broadcastToAllActiveTokens_partialFailure_continuesAndReturnsResult() throws FirebaseMessagingException {
        // given
        FcmToken token1 = createToken("fcm-token-1", "device-id-1", 1L);
        FcmToken token2 = createToken("fcm-token-2", "device-id-2", 2L);
        FirebaseMessagingException fcmException = mockFirebaseException();

        given(fcmTokenFinder.getAllActiveTokens()).willReturn(List.of(token1, token2));
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        given(firebaseMessaging.send(any()))
                .willThrow(fcmException)
                .willReturn("message-id");

        // when
        PushBroadcastResult result = fcmNotificationService.broadcastToAllActiveTokens("제목", "본문");

        // then
        assertThat(result.totalTargets()).isEqualTo(2);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failCount()).isEqualTo(1);
        assertThat(result.firebaseConfigured()).isTrue();
        then(firebaseMessaging).should(times(2)).send(any());
    }

    @DisplayName("브로드캐스트 - 전체 성공 시 결과 반환")
    @Test
    void broadcastToAllActiveTokens_allSuccess_returnsSuccessResult() throws FirebaseMessagingException {
        // given
        FcmToken token1 = createToken("fcm-token-1", "device-id-1", 1L);
        FcmToken token2 = createToken("fcm-token-2", "device-id-2", 2L);

        given(fcmTokenFinder.getAllActiveTokens()).willReturn(List.of(token1, token2));
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        given(firebaseMessaging.send(any())).willReturn("message-id");

        // when
        PushBroadcastResult result = fcmNotificationService.broadcastToAllActiveTokens("제목", "본문");

        // then
        assertThat(result.totalTargets()).isEqualTo(2);
        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.failCount()).isZero();
    }

    @DisplayName("브로드캐스트 - 영구 실패(UNREGISTERED) 토큰은 비활성화한다")
    @Test
    void broadcastToAllActiveTokens_permanentFailure_deactivatesStaleToken() throws FirebaseMessagingException {
        // given
        FcmToken token = createTokenWithId(10L, "stale-token", "device-id-1", 1L);
        FirebaseMessagingException permanent = mockFirebaseException(MessagingErrorCode.UNREGISTERED);

        given(fcmTokenFinder.getAllActiveTokens()).willReturn(List.of(token));
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        given(firebaseMessaging.send(any())).willThrow(permanent);

        // when
        PushBroadcastResult result = fcmNotificationService.broadcastToAllActiveTokens("제목", "본문");

        // then
        assertThat(result.failCount()).isEqualTo(1);
        then(fcmTokenCommandService).should(times(1)).deactivateByIds(List.of(10L));
    }

    @DisplayName("브로드캐스트 - 일시 실패(INTERNAL) 토큰은 비활성화하지 않는다")
    @Test
    void broadcastToAllActiveTokens_transientFailure_keepsToken() throws FirebaseMessagingException {
        // given
        FcmToken token = createTokenWithId(11L, "fcm-token-1", "device-id-1", 1L);
        FirebaseMessagingException transientErr = mockFirebaseException(MessagingErrorCode.INTERNAL);

        given(fcmTokenFinder.getAllActiveTokens()).willReturn(List.of(token));
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        given(firebaseMessaging.send(any())).willThrow(transientErr);

        // when
        fcmNotificationService.broadcastToAllActiveTokens("제목", "본문");

        // then
        then(fcmTokenCommandService).should(never()).deactivateByIds(any());
    }

    private FcmToken createToken(String fcmToken, String deviceId, Long userId) {
        FcmTokenRegisterRequest request = FcmTokenRegisterRequest.of(
                fcmToken, DeviceType.ANDROID, deviceId, "Galaxy S23", "Android 14", "1.0.0"
        );
        FcmToken token = FcmToken.register(request);
        token.linkUser(userId);
        return token;
    }

    private FcmToken createTokenWithId(Long id, String fcmToken, String deviceId, Long userId) {
        FcmToken token = createToken(fcmToken, deviceId, userId);
        ReflectionTestUtils.setField(token, "id", id);
        return token;
    }

    private FirebaseMessagingException mockFirebaseException() {
        return mockFirebaseException(MessagingErrorCode.INTERNAL);
    }

    private FirebaseMessagingException mockFirebaseException(MessagingErrorCode code) {
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        given(exception.getMessagingErrorCode()).willReturn(code);
        return exception;
    }
}
