package com.souzip.api.domain.user.entity;

import com.souzip.api.domain.auth.dto.OAuthUserInfo;
import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
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
    },
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_provider", columnList = "provider, provider_id"),
        @Index(name = "idx_deleted", columnList = "deleted"),
        @Index(name = "idx_email", columnList = "email")
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

    private String nickname;

    private String email;

    private String profileImageUrl;

    private String transferIdentifier;

    private LocalDateTime lastLoginAt;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean onboardingCompleted;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_category",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private Boolean deleted;

    private LocalDateTime restoredAt;

    @PrePersist
    protected void onCreate() {
        ensureUserId();
    }

    public static User of(
        Provider provider,
        String providerId,
        String name,
        String nickname,
        String email,
        String profileImageUrl
    ) {
        return User.builder()
            .userId(UUID.randomUUID().toString())
            .provider(provider)
            .providerId(providerId)
            .name(name)
            .nickname(nickname)
            .email(email)
            .profileImageUrl(profileImageUrl)
            .onboardingCompleted(false)
            .categories(new HashSet<>())
            .deleted(false)
            .build();
    }

    public static User of(Provider provider, OAuthUserInfo oauthUserInfo) {
        return User.of(
            provider,
            oauthUserInfo.getProviderId(),
            oauthUserInfo.getName(),
            null,
            oauthUserInfo.getEmail(),
            oauthUserInfo.getProfileImageUrl()
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

    public void restore(String originalName) {
        this.name = originalName;
        this.nickname = null;
        this.deleted = false;
        this.deletedAt = null;
        this.restoredAt = LocalDateTime.now();
        this.onboardingCompleted = false;
        this.categories.clear();
    }

    public boolean needsOnboarding() {
        return !onboardingCompleted;
    }

    public void completeOnboarding(String nickname, String profileImageUrl, Set<Category> categories) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.categories = categories;
        this.onboardingCompleted = true;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }

    public void updateTransferIdentifier(String transferIdentifier) {
        this.transferIdentifier = transferIdentifier;
    }

    public void updateProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
