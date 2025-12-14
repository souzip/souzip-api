package com.souzip.api.domain.user.service;

import com.souzip.api.domain.auth.entity.RefreshToken;
import com.souzip.api.domain.auth.repository.RefreshTokenRepository;
import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.user.dto.OnboardingRequest;
import com.souzip.api.domain.user.dto.OnboardingResponse;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    @DisplayName("온보딩을 완료하면 User가 업데이트된다.")
    void completeOnboarding_success() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "카카오사용자", "카카오사용자",
            "test@kakao.com", "https://kakao.com/profile.jpg");
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
            "수집",
            "https://cdn.souzip.com/characters/character1.png",
            List.of("FOOD_SNACK", "BEAUTY_HEALTH", "FASHION_ACCESSORY")
        );

        given(userRepository.findById(1L))
            .willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);
        given(spyUser.getUserId()).willReturn("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        given(spyUser.getNickname()).willReturn("수집");
        given(spyUser.getProfileImageUrl()).willReturn("https://cdn.souzip.com/characters/character1.png");
        given(spyUser.getEmail()).willReturn("test@kakao.com");

        // when
        OnboardingResponse response = userService.completeOnboarding(1L, request);

        // then
        assertThat(response.userId()).isEqualTo("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        assertThat(response.nickname()).isEqualTo("수집");
        assertThat(response.profileImageUrl()).isEqualTo("https://cdn.souzip.com/characters/character1.png");
        assertThat(response.email()).isEqualTo("test@kakao.com");
        assertThat(response.categories()).hasSize(3);

        verify(spyUser).completeOnboarding(
            eq("수집"),
            eq("https://cdn.souzip.com/characters/character1.png"),
            any()
        );
    }

    @Test
    @DisplayName("이미 온보딩을 완료한 사용자는 다시 온보딩할 수 없다.")
    void completeOnboarding_alreadyCompleted() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "수집", "수집",
            "test@kakao.com", "https://cdn.souzip.com/characters/character1.png");
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
            "새닉네임",
            "https://cdn.souzip.com/characters/character2.png",
            List.of("FOOD_SNACK")
        );

        given(userRepository.findById(1L))
            .willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(false);  // 이미 완료!

        // when & then
        assertThatThrownBy(() -> userService.completeOnboarding(1L, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("이미 온보딩을 완료한 사용자입니다.");

        verify(spyUser, never()).completeOnboarding(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("유효하지 않은 카테고리로 온보딩 시 에러가 발생한다.")
    void completeOnboarding_invalidCategory() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "카카오사용자", "카카오사용자",
            "test@kakao.com", null);
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
            "수집",
            "https://cdn.souzip.com/characters/character1.png",
            List.of("INVALID_CATEGORY", "FOOD_SNACK")
        );

        given(userRepository.findById(1L))
            .willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.completeOnboarding(1L, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("유효하지 않은 카테고리입니다.");

        verify(spyUser, never()).completeOnboarding(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 온보딩 시 에러가 발생한다.")
    void completeOnboarding_userNotFound() {
        // given
        OnboardingRequest request = new OnboardingRequest(
            "수집",
            "https://cdn.souzip.com/characters/character1.png",
            List.of("FOOD_SNACK")
        );

        given(userRepository.findById(999L))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.completeOnboarding(999L, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("온보딩 시 카테고리가 올바르게 변환된다.")
    void completeOnboarding_categoryConversion() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "카카오사용자", "카카오사용자",
            "test@kakao.com", null);
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
            "수집",
            "https://cdn.souzip.com/characters/character1.png",
            List.of("FOOD_SNACK", "BEAUTY_HEALTH")
        );

        given(userRepository.findById(1L))
            .willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);
        given(spyUser.getUserId()).willReturn("a1b2c3d4");
        given(spyUser.getNickname()).willReturn("수집");
        given(spyUser.getProfileImageUrl()).willReturn("https://cdn.souzip.com/characters/character1.png");
        given(spyUser.getEmail()).willReturn("test@kakao.com");

        // when
        OnboardingResponse response = userService.completeOnboarding(1L, request);

        // then
        List<String> categoryNames = response.categories().stream()
            .map(CategoryDto::name)
            .toList();

        assertThat(categoryNames).containsExactlyInAnyOrder("FOOD_SNACK", "BEAUTY_HEALTH");

        List<String> categoryLabels = response.categories().stream()
            .map(CategoryDto::label)
            .toList();

        assertThat(categoryLabels).contains("먹거리·간식", "뷰티·헬스");
    }

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
