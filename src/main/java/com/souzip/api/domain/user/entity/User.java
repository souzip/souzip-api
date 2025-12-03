package com.souzip.api.domain.user.entity;

import com.souzip.api.domain.auth.dto.OAuthUserInfo;
import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "\"user\"",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
    }
)
@SQLDelete(sql = "UPDATE \"user\" SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
@Entity
public class User extends BaseEntity {

    @Column(unique = true, nullable = false, length = 36)
    private String userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String nickname;

    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private Boolean deleted;

    @PrePersist
    protected void onCreate() {
        ensureUserId();
    }

    public static User of(Provider provider, String providerId, String name, String nickname) {
        return User.builder()
            .userId(UUID.randomUUID().toString())
            .provider(provider)
            .providerId(providerId)
            .name(name)
            .nickname(nickname)
            .deleted(false)
            .build();
    }

    public static User of(Provider provider, OAuthUserInfo oauthUserInfo) {
        String name = oauthUserInfo.getName();
        String originalName = name;
        String defaultNickname = name;

        return User.of(
            provider,
            oauthUserInfo.getProviderId(),
            originalName,
            defaultNickname
        );
    }

    private void ensureUserId() {
        if (isUserIdAbsent()) {
            this.userId = UUID.randomUUID().toString();
        }
    }

    private boolean isUserIdAbsent() {
        return this.userId == null;
    }
}
