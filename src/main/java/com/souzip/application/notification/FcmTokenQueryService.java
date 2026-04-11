package com.souzip.application.notification;

import com.souzip.application.notification.provided.FcmTokenFinder;
import com.souzip.application.notification.required.FcmTokenRepository;
import com.souzip.domain.notification.FcmToken;
import com.souzip.domain.notification.FcmTokenNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FcmTokenQueryService implements FcmTokenFinder {

    private final FcmTokenRepository fcmTokenRepository;

    @Override
    public FcmToken getByDeviceId(String deviceId) {
        return fcmTokenRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> FcmTokenNotFoundException.byDeviceId(deviceId));
    }

    @Override
    public FcmToken getByToken(String token) {
        return fcmTokenRepository.findByToken(token)
                .orElseThrow(() -> FcmTokenNotFoundException.byToken(token));
    }

    @Override
    public List<FcmToken> getActiveTokensByUserId(Long userId) {
        return fcmTokenRepository.findByUserIdAndActiveTrue(userId);
    }

    @Override
    public List<FcmToken> getAllActiveTokens() {
        return fcmTokenRepository.findAllByActiveTrue();
    }

    @Override
    public List<FcmToken> getAllActiveTokensWithMarketingConsent() {
        return fcmTokenRepository.findAllActiveWithMarketingConsent();
    }
}
