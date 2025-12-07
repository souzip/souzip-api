package com.souzip.api.domain.user.entity;

import com.souzip.api.domain.auth.dto.OAuthUserInfo;
import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

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
@Entity
public class User extends BaseEntity {

    private static final String ANONYMIZED_VALUE = "탈퇴한사용자";

    @Column(unique = true, nullable = false, length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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

    private LocalDateTime restoredAt;

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
        return User.of(
            provider,
            oauthUserInfo.getProviderId(),
            name,
            name
        );
    }

    private void ensureUserId() {
        if (this.userId == null) {
            this.userId = UUID.randomUUID().toString();
        }
    }

    public void anonymize() {
        this.name = ANONYMIZED_VALUE;
        this.nickname = ANONYMIZED_VALUE;
    }

    public void restore(String originalName, String originalNickname) {
        this.name = originalName;
        this.nickname = originalNickname;
        this.deleted = false;
        this.deletedAt = null;
        this.restoredAt = LocalDateTime.now();
    }

    public boolean isRecentlyCreated(LocalDateTime threshold) {
        return this.getCreatedAt().isAfter(threshold);
    }

    public boolean isRecentlyRestored(LocalDateTime threshold) {
        return this.restoredAt != null && this.restoredAt.isAfter(threshold);
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }
}
