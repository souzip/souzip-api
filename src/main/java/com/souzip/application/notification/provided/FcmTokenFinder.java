package com.souzip.application.notification.provided;

import com.souzip.domain.notification.FcmToken;
import java.util.List;

public interface FcmTokenFinder {

    FcmToken getByDeviceId(String deviceId);

    FcmToken getByToken(String token);

    List<FcmToken> getActiveTokensByUserId(Long userId);

    List<FcmToken> getAllActiveTokens();
}
