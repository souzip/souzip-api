package com.souzip.api.domain.user.service;

import com.souzip.api.domain.auth.entity.RefreshToken;
import com.souzip.api.domain.auth.repository.RefreshTokenRepository;
import com.souzip.api.domain.user.entity.Provider;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.domain.user.repository.UserRepository;
import com.souzip.api.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원탈퇴 시 User는 익명화되고 soft delete된다")
    void withdraw_success() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "테스트유저", "테스트", null, null);

        RefreshToken refreshToken = RefreshToken.of(
            user,
            "refresh_token",
            LocalDateTime.now().plusDays(30)
        );

        given(userRepository.findById(1L))
            .willReturn(Optional.of(user));
        given(refreshTokenRepository.findByUser(user))
            .willReturn(Optional.of(refreshToken));

        // when
        userService.withdraw(1L);

        // then
        assertThat(user.getName()).isEqualTo("탈퇴한사용자");
        assertThat(user.getNickname()).isEqualTo("탈퇴한사용자");

        verify(refreshTokenRepository).delete(refreshToken);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 회원탈퇴 시 에러가 발생한다.")
    void withdraw_withNotExistUser_throwsException() {
        // given
        given(userRepository.findById(999L))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.withdraw(999L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("Refresh Token이 없어도 회원탈퇴는 성공한다.")
    void withdraw_withoutRefreshToken_success() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "테스트유저", "테스트", null, null);
        User spyUser = spy(user);

        given(userRepository.findById(1L))
            .willReturn(Optional.of(spyUser));

        given(refreshTokenRepository.findByUser(spyUser))
            .willReturn(Optional.empty());
        // when
        userService.withdraw(1L);

        // then
        verify(spyUser).anonymize();
        verify(userRepository).delete(spyUser);
        verify(refreshTokenRepository, never()).delete(any());
    }
}
