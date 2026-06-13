package com.souzip.application.notification;

import com.souzip.application.notification.required.FcmTokenRepository;
import com.souzip.domain.notification.FcmToken;
import com.souzip.domain.notification.FcmTokenRegisterRequest;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FcmTokenCommandService {

    private final FcmTokenRepository fcmTokenRepository;

    // 로그인 사용자 기준으로 FCM 토큰을 등록하거나 동일 디바이스·토큰에 맞춰 갱신합니다.
    public FcmToken registerOrUpdate(Long userId, FcmTokenRegisterRequest request) {
        return fcmTokenRepository.findByToken(request.fcmToken())
                .map(existing -> syncExistingToken(existing, userId, request))
                .orElseGet(() -> fcmTokenRepository.findByUserIdAndDeviceId(userId, request.deviceId())
                        .map(existing -> updateExistingDeviceRow(existing, request, userId))
                        .orElseGet(() -> createNew(userId, request)));
    }

    private FcmToken syncExistingToken(FcmToken existing, Long userId, FcmTokenRegisterRequest request) {
        // 같은 기기(deviceId)로 이미 등록된 다른 row 가 있으면 unique(user_id, device_id) 충돌을 막기 위해 먼저 제거합니다.
        // (같은 기기에서 계정을 전환하거나 FCM 토큰이 재할당되는 경우 발생)
        fcmTokenRepository.deleteByUserIdAndDeviceIdExcludingId(userId, request.deviceId(), existing.getId());
        existing.linkUser(userId);
        existing.syncDeviceIdentity(request.deviceType(), request.deviceId());
        existing.updateDeviceInfo(request.deviceModel(), request.osVersion(), request.appVersion());
        existing.activate();
        return fcmTokenRepository.save(existing);
    }

    private FcmToken updateExistingDeviceRow(FcmToken existing, FcmTokenRegisterRequest request, Long userId) {
        existing.updateToken(request.fcmToken());
        existing.syncDeviceIdentity(request.deviceType(), request.deviceId());
        existing.updateDeviceInfo(request.deviceModel(), request.osVersion(), request.appVersion());
        existing.linkUser(userId);
        existing.activate();
        return fcmTokenRepository.save(existing);
    }

    private FcmToken createNew(Long userId, FcmTokenRegisterRequest request) {
        FcmToken created = FcmToken.register(request);
        created.linkUser(userId);
        return fcmTokenRepository.save(created);
    }

    // 해당 디바이스의 푸시 토큰을 비활성화합니다.
    public void deactivateByDevice(Long userId, String deviceId) {
        fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId)
                .ifPresent(token -> {
                    token.deactivate();
                    fcmTokenRepository.save(token);
                });
    }

    // 회원 탈퇴 시 해당 사용자의 모든 활성 FCM 토큰을 비활성화합니다.
    public void deactivateAllByUserId(Long userId) {
        List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndActiveTrue(userId);
        tokens.forEach(token -> {
            token.deactivate();
            fcmTokenRepository.save(token);
        });
    }

    // 영구 실패(UNREGISTERED 등)로 더 이상 유효하지 않은 토큰들을 일괄 비활성화합니다.
    public void deactivateByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return;
        }
        List<FcmToken> tokens = fcmTokenRepository.findAllByIdIn(ids);
        tokens.forEach(token -> {
            token.deactivate();
            fcmTokenRepository.save(token);
        });
    }
}
