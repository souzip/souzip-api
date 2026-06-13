package com.souzip.application.notification;

import com.souzip.application.notification.required.FcmTokenRepository;
import com.souzip.domain.notification.DeviceType;
import com.souzip.domain.notification.FcmToken;
import com.souzip.domain.notification.FcmTokenRegisterRequest;
import com.souzip.shared.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

// 실제 unique(user_id, device_id) / unique(token) 제약이 적용되는 JPA 슬라이스에서
// 토큰 재할당·계정 전환 시 충돌이 발생하지 않는지 검증합니다.
@DataJpaTest
@EnableJpaAuditing
@Import({FcmTokenCommandService.class, QuerydslConfig.class})
class FcmTokenCommandServicePersistenceTest {

    @Autowired
    private FcmTokenCommandService fcmTokenCommandService;

    @Autowired
    private FcmTokenRepository fcmTokenRepository;

    @DisplayName("같은 기기의 토큰이 다른 row로 재할당돼도 unique 충돌 없이 갱신된다")
    @Test
    void registerOrUpdate_tokenReassignedToExistingDevice_noUniqueViolation() {
        // given: user1 이 기기 D 에 기존 토큰으로 등록
        fcmTokenCommandService.registerOrUpdate(1L, request("token-old", "device-D"));
        // 다른 기기에 token-new 가 이미 존재
        fcmTokenCommandService.registerOrUpdate(1L, request("token-new", "device-OTHER"));

        // when & then: 기기 D 가 token-new 로 재등록돼도 (user1, device-D) unique 충돌이 없어야 한다
        assertThatCode(() ->
                fcmTokenCommandService.registerOrUpdate(1L, request("token-new", "device-D"))
        ).doesNotThrowAnyException();

        // (user1, device-D) row 는 하나만 남고 token-new 를 가리킨다
        FcmToken deviceRow = fcmTokenRepository.findByUserIdAndDeviceId(1L, "device-D").orElseThrow();
        assertThat(deviceRow.getToken()).isEqualTo("token-new");
        assertThat(deviceRow.isActive()).isTrue();

        // token-new 를 들고 있던 기존 device-OTHER row 는 정리되어 활성 토큰은 1개만 남는다
        List<FcmToken> active = fcmTokenRepository.findByUserIdAndActiveTrue(1L);
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getDeviceId()).isEqualTo("device-D");
    }

    @DisplayName("다른 계정으로 전환해도 기기 토큰이 새 사용자로 충돌 없이 이전된다")
    @Test
    void registerOrUpdate_accountSwitchOnSameDevice_noUniqueViolation() {
        // given: user1 이 기기 D 에 등록 후, 같은 토큰이 user2 row 로도 존재하는 상황을 만든다
        fcmTokenCommandService.registerOrUpdate(1L, request("token-shared", "device-D"));

        // when: user2 가 같은 기기(device-D)·같은 토큰으로 로그인(계정 전환)
        assertThatCode(() ->
                fcmTokenCommandService.registerOrUpdate(2L, request("token-shared", "device-D"))
        ).doesNotThrowAnyException();

        // then: 토큰은 user2 로 이전되고 (user2, device-D) 하나만 존재
        FcmToken row = fcmTokenRepository.findByToken("token-shared").orElseThrow();
        assertThat(row.getUserId()).isEqualTo(2L);
        assertThat(row.getDeviceId()).isEqualTo("device-D");
        assertThat(fcmTokenRepository.findByUserIdAndActiveTrue(1L)).isEmpty();
    }

    private FcmTokenRegisterRequest request(String token, String deviceId) {
        return FcmTokenRegisterRequest.of(
                token, DeviceType.ANDROID, deviceId, "Galaxy S23", "Android 14", "1.0.0"
        );
    }
}
