package com.souzip.application.notification.required;

import com.souzip.domain.notification.FcmToken;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface FcmTokenRepository extends Repository<FcmToken, Long> {

    FcmToken save(FcmToken fcmToken);

    List<FcmToken> findAllByIdIn(Collection<Long> ids);

    Optional<FcmToken> findByToken(String token);

    Optional<FcmToken> findByDeviceId(String deviceId);

    Optional<FcmToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    // 같은 (user_id, device_id) 의 다른 row 를 즉시(벌크) 삭제합니다.
    // 토큰 재할당 시 unique(user_id, device_id) 충돌을 막기 위해 update 전에 호출합니다.
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM FcmToken t WHERE t.userId = :userId AND t.deviceId = :deviceId AND t.id <> :excludeId")
    void deleteByUserIdAndDeviceIdExcludingId(
            @Param("userId") Long userId,
            @Param("deviceId") String deviceId,
            @Param("excludeId") Long excludeId
    );

    List<FcmToken> findByUserIdAndActiveTrue(Long userId);

    List<FcmToken> findAllByActiveTrue();

    void delete(FcmToken fcmToken);

    void deleteByDeviceId(String deviceId);

    boolean existsByToken(String token);

    boolean existsByDeviceId(String deviceId);
}
