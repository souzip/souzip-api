package com.souzip.application.notification;

import com.souzip.application.notification.required.FcmTokenRepository;
import com.souzip.domain.notification.DeviceType;
import com.souzip.domain.notification.FcmToken;
import com.souzip.domain.notification.FcmTokenRegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FcmTokenCommandServiceTest {

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @InjectMocks
    private FcmTokenCommandService fcmTokenCommandService;

    @DisplayName("동일 FCM 토큰이 이미 존재하면 사용자를 연결하고 활성화한다")
    @Test
    void registerOrUpdate_existingToken_linksUserAndActivates() {
        // given
        FcmToken existing = createToken("fcm-token-123", "device-id-123", 2L);
        existing.deactivate();

        FcmTokenRegisterRequest request = createRequest("fcm-token-123", "device-id-123");

        given(fcmTokenRepository.findByToken("fcm-token-123")).willReturn(Optional.of(existing));
        given(fcmTokenRepository.save(existing)).willReturn(existing);

        // when
        FcmToken result = fcmTokenCommandService.registerOrUpdate(1L, request);

        // then
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.isActive()).isTrue();
        then(fcmTokenRepository).should(times(1)).save(existing);
    }

    @DisplayName("동일 디바이스가 있으면 FCM 토큰을 갱신한다")
    @Test
    void registerOrUpdate_sameDevice_updatesToken() {
        // given
        FcmToken existing = createToken("old-fcm-token", "device-id-123", 1L);
        FcmTokenRegisterRequest request = createRequest("new-fcm-token", "device-id-123");

        given(fcmTokenRepository.findByToken("new-fcm-token")).willReturn(Optional.empty());
        given(fcmTokenRepository.findByUserIdAndDeviceId(1L, "device-id-123")).willReturn(Optional.of(existing));
        given(fcmTokenRepository.save(existing)).willReturn(existing);

        // when
        FcmToken result = fcmTokenCommandService.registerOrUpdate(1L, request);

        // then
        assertThat(result.getToken()).isEqualTo("new-fcm-token");
        assertThat(result.isActive()).isTrue();
    }

    @DisplayName("신규 토큰이면 새 레코드를 생성한다")
    @Test
    void registerOrUpdate_newToken_creates() {
        // given
        FcmTokenRegisterRequest request = createRequest("brand-new-token", "device-id-new");

        given(fcmTokenRepository.findByToken("brand-new-token")).willReturn(Optional.empty());
        given(fcmTokenRepository.findByUserIdAndDeviceId(1L, "device-id-new")).willReturn(Optional.empty());
        given(fcmTokenRepository.save(any(FcmToken.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        FcmToken result = fcmTokenCommandService.registerOrUpdate(1L, request);

        // then
        assertThat(result.getToken()).isEqualTo("brand-new-token");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.isActive()).isTrue();
    }

    @DisplayName("디바이스 ID로 토큰을 비활성화한다")
    @Test
    void deactivateByDevice_deactivatesToken() {
        // given
        FcmToken token = createToken("fcm-token-123", "device-id-123", 1L);

        given(fcmTokenRepository.findByUserIdAndDeviceId(1L, "device-id-123")).willReturn(Optional.of(token));
        given(fcmTokenRepository.save(token)).willReturn(token);

        // when
        fcmTokenCommandService.deactivateByDevice(1L, "device-id-123");

        // then
        assertThat(token.isActive()).isFalse();
        then(fcmTokenRepository).should(times(1)).save(token);
    }

    @DisplayName("해당 디바이스가 없으면 아무것도 하지 않는다")
    @Test
    void deactivateByDevice_noToken_doesNothing() {
        // given
        given(fcmTokenRepository.findByUserIdAndDeviceId(1L, "nonexistent-device")).willReturn(Optional.empty());

        // when
        fcmTokenCommandService.deactivateByDevice(1L, "nonexistent-device");

        // then
        then(fcmTokenRepository).should(never()).save(any());
    }

    @DisplayName("회원 탈퇴 시 해당 사용자의 모든 활성 토큰을 비활성화한다")
    @Test
    void deactivateAllByUserId_deactivatesAllActiveTokens() {
        // given
        FcmToken token1 = createToken("fcm-token-1", "device-id-1", 1L);
        FcmToken token2 = createToken("fcm-token-2", "device-id-2", 1L);

        given(fcmTokenRepository.findByUserIdAndActiveTrue(1L)).willReturn(List.of(token1, token2));
        given(fcmTokenRepository.save(any(FcmToken.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        fcmTokenCommandService.deactivateAllByUserId(1L);

        // then
        assertThat(token1.isActive()).isFalse();
        assertThat(token2.isActive()).isFalse();
        then(fcmTokenRepository).should(times(2)).save(any(FcmToken.class));
    }

    @DisplayName("회원 탈퇴 시 활성 토큰이 없으면 아무것도 하지 않는다")
    @Test
    void deactivateAllByUserId_noActiveTokens_doesNothing() {
        // given
        given(fcmTokenRepository.findByUserIdAndActiveTrue(1L)).willReturn(List.of());

        // when
        fcmTokenCommandService.deactivateAllByUserId(1L);

        // then
        then(fcmTokenRepository).should(never()).save(any());
    }

    @DisplayName("영구 실패 토큰들을 ID로 일괄 비활성화한다")
    @Test
    void deactivateByIds_deactivatesGivenTokens() {
        // given
        FcmToken token1 = createToken("fcm-token-1", "device-id-1", 1L);
        FcmToken token2 = createToken("fcm-token-2", "device-id-2", 2L);

        given(fcmTokenRepository.findAllByIdIn(List.of(10L, 20L))).willReturn(List.of(token1, token2));
        given(fcmTokenRepository.save(any(FcmToken.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        fcmTokenCommandService.deactivateByIds(List.of(10L, 20L));

        // then
        assertThat(token1.isActive()).isFalse();
        assertThat(token2.isActive()).isFalse();
        then(fcmTokenRepository).should(times(2)).save(any(FcmToken.class));
    }

    @DisplayName("비활성화할 ID가 비어있으면 조회조차 하지 않는다")
    @Test
    void deactivateByIds_emptyIds_doesNothing() {
        // when
        fcmTokenCommandService.deactivateByIds(List.of());

        // then
        then(fcmTokenRepository).should(never()).findAllByIdIn(any());
        then(fcmTokenRepository).should(never()).save(any());
    }

    private FcmToken createToken(String fcmToken, String deviceId, Long userId) {
        FcmTokenRegisterRequest request = createRequest(fcmToken, deviceId);
        FcmToken token = FcmToken.register(request);
        token.linkUser(userId);
        return token;
    }

    private FcmTokenRegisterRequest createRequest(String fcmToken, String deviceId) {
        return FcmTokenRegisterRequest.of(
                fcmToken, DeviceType.ANDROID, deviceId, "Galaxy S23", "Android 14", "1.0.0"
        );
    }
}
