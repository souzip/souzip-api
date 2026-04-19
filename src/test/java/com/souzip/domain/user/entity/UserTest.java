package com.souzip.domain.user.entity;

import com.souzip.shared.domain.Provider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("anonymize 호출 시 개인정보가 익명화된다.")
    void anonymize_success() {
        User user = User.of(Provider.KAKAO, "kakao123", "test@kakao.com");

        user.anonymize();

        assertThat(user.getNickname()).isEqualTo("탈퇴한사용자");
    }

    @Test
    @DisplayName("restore 호출 시 nickname은 빈값으로 초기화된다.")
    void restore_success() {
        User user = User.of(Provider.KAKAO, "kakao123", "test@kakao.com");

        user.restore("테스트유저");

        assertThat(user.getNickname()).isEmpty();
        assertThat(user.getDeleted()).isFalse();
        assertThat(user.getRestoredAt()).isNotNull();
        assertThat(user.getDeletedAt()).isNull();
        assertThat(user.isOnboardingCompleted()).isFalse();
        assertThat(user.getCategories()).isEmpty();
    }

    @Test
    @DisplayName("updateLastLoginAt 호출 시 lastLoginAt이 현재 시간으로 설정된다.")
    void updateLastLoginAt_success() {
        User user = User.of(Provider.KAKAO, "kakao123", "test@kakao.com");
        LocalDateTime before = LocalDateTime.now();

        user.updateLastLoginAt();

        assertThat(user.getLastLoginAt()).isNotNull();
        assertThat(user.getLastLoginAt()).isAfterOrEqualTo(before);
        assertThat(user.getLastLoginAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}