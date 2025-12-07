package com.souzip.api.domain.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("anonymize 호출 시 개인정보가 익명화된다.")
    void anonymize_success() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "홍길동", "길동이");

        // when
        user.anonymize();

        // then
        assertThat(user.getName()).isEqualTo("탈퇴한사용자");
        assertThat(user.getNickname()).isEqualTo("탈퇴한사용자");
    }

    @Test
    @DisplayName("restore 호출 시 개인정보가 복구되고 restoredAt이 설정된다.")
    void restore_success() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "탈퇴한사용자", "탈퇴한사용자");

        // when
        user.restore("홍길동", "길동이");

        // then
        assertThat(user.getName()).isEqualTo("홍길동");
        assertThat(user.getNickname()).isEqualTo("길동이");
        assertThat(user.getDeleted()).isFalse();
        assertThat(user.getRestoredAt()).isNotNull();
        assertThat(user.getDeletedAt()).isNull();
    }
}
