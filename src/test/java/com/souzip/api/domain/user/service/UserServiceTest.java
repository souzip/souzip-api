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
    @DisplayName("회원탈퇴 시 User는 soft delete되고 Refresh Token은 삭제된다.")
    void withdraw_success() {
        User user = User.of(Provider.KAKAO, "kakao123", "테스트유저", "테스트");
        User spyUser = spy(user);

        RefreshToken refreshToken = RefreshToken.of(
            spyUser,
            "refresh_token",
            LocalDateTime.now().plusDays(30)
        );

        given(userRepository.findById(1L))
            .willReturn(Optional.of(spyUser));
        given(refreshTokenRepository.findByUser(spyUser))
            .willReturn(Optional.of(refreshToken));

        // when
        userService.withdraw(1L);

        // then
        verify(refreshTokenRepository).delete(refreshToken);
        verify(userRepository).delete(spyUser);
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
}
