package com.souzip.api.domain.user.entity;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("anonymize 호출 시 개인정보가 익명화된다.")
    void anonymize_success() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "테스트유저", "테스트", "test@kakao.com", null);

        // when
        user.anonymize();

        // then
        assertThat(user.getName()).isEqualTo("탈퇴한사용자");
        assertThat(user.getNickname()).isEqualTo("탈퇴한사용자");
    }

    @Test
    @DisplayName("restore 호출 시 name이 복구되고 nickname은 null로 초기화된다.")
    void restore_success() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "탈퇴한사용자", "탈퇴한사용자", "test@kakao.com", null);

        // when
        user.restore("테스트유저");

        // then
        assertThat(user.getName()).isEqualTo("테스트유저");
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
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "테스트", "테스트", "test@kakao.com", null);
        LocalDateTime before = LocalDateTime.now();

        // when
        user.updateLastLoginAt();

        // then
        assertThat(user.getLastLoginAt()).isNotNull();
        assertThat(user.getLastLoginAt()).isAfterOrEqualTo(before);
        assertThat(user.getLastLoginAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
