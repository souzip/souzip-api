package com.souzip.domain.user.service;

import com.souzip.domain.auth.entity.RefreshToken;
import com.souzip.domain.auth.repository.RefreshTokenRepository;
import com.souzip.domain.user.dto.NicknameCheckResponse;
import com.souzip.domain.user.dto.OnboardingRequest;
import com.souzip.domain.user.dto.OnboardingResponse;
import com.souzip.domain.user.entity.Provider;
import com.souzip.domain.user.entity.User;
import com.souzip.domain.user.entity.UserAgreement;
import com.souzip.domain.user.repository.UserAgreementRepository;
import com.souzip.domain.user.repository.UserRepository;
import com.souzip.global.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAgreementRepository userAgreementRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private ProfileImageService profileImageService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("사용 가능한 닉네임을 확인한다.")
    void checkNickname_available() {
        // given
        String nickname = "새로운닉네임";
        given(userRepository.existsByNickname(nickname)).willReturn(false);

        // when
        NicknameCheckResponse response = userService.checkNickname(nickname);

        // then
        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getMessage()).isEqualTo("사용 가능한 닉네임입니다.");
        verify(userRepository).existsByNickname(nickname);
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임을 확인한다.")
    void checkNickname_unavailable() {
        // given
        String nickname = "중복닉네임";
        given(userRepository.existsByNickname(nickname)).willReturn(true);

        // when
        NicknameCheckResponse response = userService.checkNickname(nickname);

        // then
        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getMessage()).isEqualTo("이미 사용 중인 닉네임입니다.");
        verify(userRepository).existsByNickname(nickname);
    }

    @Test
    @DisplayName("온보딩 시 닉네임이 중복되면 에러가 발생한다.")
    void completeOnboarding_nicknameAlreadyExists() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "카카오사용자", "카카오사용자",
            "test@kakao.com", null);
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
            true, true, true, true, false,
            "중복닉네임",
            "red",
            List.of("FOOD_SNACK")
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);
        given(userRepository.existsByNickname("중복닉네임")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.completeOnboarding(1L, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("이미 사용 중인 닉네임입니다.");

        verify(userRepository).existsByNickname("중복닉네임");
        verify(userAgreementRepository, never()).save(any());
        verify(spyUser, never()).completeOnboarding(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("온보딩을 완료하면 User와 UserAgreement가 저장된다.")
    void completeOnboarding_success() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "카카오사용자", "카카오사용자",
            "test@kakao.com", "https://kakao.com/profile.jpg");
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
            true,
            true,
            true,
            true,
            false,
            "수집",
            "red",
            List.of("FOOD_SNACK", "BEAUTY_HEALTH", "FASHION_ACCESSORY")
        );

        String expectedImageUrl = "https://kr.object.ncloudstorage.com/souzip-dev-images/profile/red.svg";

        UserAgreement agreement = UserAgreement.of(
            spyUser,
            true, true, true, true, false
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);
        given(userAgreementRepository.existsByUser(spyUser)).willReturn(false);
        given(userAgreementRepository.save(any(UserAgreement.class))).willReturn(agreement);
        given(profileImageService.resolveProfileImageUrl("red")).willReturn(expectedImageUrl);
        given(spyUser.getUserId()).willReturn("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        given(spyUser.getNickname()).willReturn("수집");
        given(spyUser.getProfileImageUrl()).willReturn(expectedImageUrl);

        // when
        OnboardingResponse response = userService.completeOnboarding(1L, request);

        // then
        assertThat(response.userId()).isEqualTo("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        assertThat(response.nickname()).isEqualTo("수집");
        assertThat(response.profileImageUrl()).isEqualTo(expectedImageUrl);
        assertThat(response.categories()).hasSize(3);
        assertThat(response.agreements()).isNotNull();
        assertThat(response.agreements().ageVerified()).isTrue();
        assertThat(response.agreements().marketingConsent()).isFalse();

        verify(userAgreementRepository).save(any(UserAgreement.class));
        verify(profileImageService).resolveProfileImageUrl("red");
        verify(spyUser).completeOnboarding(eq("수집"), eq(expectedImageUrl), any());
    }

    @Test
    @DisplayName("필수 약관에 동의하지 않으면 온보딩에 실패한다.")
    void completeOnboarding_requiredAgreementNotChecked() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "카카오사용자", "카카오사용자",
            "test@kakao.com", null);
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
            true,
            false,
            true,
            true,
            false,
            "수집",
            "red",
            List.of("FOOD_SNACK")
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.completeOnboarding(1L, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("필수 약관에 모두 동의해야 합니다.");

        verify(userAgreementRepository, never()).save(any());
        verify(spyUser, never()).completeOnboarding(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("이미 약관에 동의한 사용자는 중복 저장되지 않는다.")
    void completeOnboarding_agreementAlreadyExists() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "카카오사용자", "카카오사용자",
            "test@kakao.com", null);
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
            true, true, true, true, false,
            "수집",
            "red",
            List.of("FOOD_SNACK")
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);
        given(userAgreementRepository.existsByUser(spyUser)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.completeOnboarding(1L, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("이미 약관에 동의한 사용자입니다.");

        verify(userAgreementRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 온보딩을 완료한 사용자는 다시 온보딩할 수 없다.")
    void completeOnboarding_alreadyCompleted() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "수집", "수집",
            "test@kakao.com", "https://kr.object.ncloudstorage.com/souzip-dev-images/profile/red.svg");
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
            true, true, true, true, false,
            "새닉네임",
            "blue",
            List.of("FOOD_SNACK")
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.completeOnboarding(1L, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("이미 온보딩을 완료한 사용자입니다.");

        verify(profileImageService, never()).resolveProfileImageUrl(anyString());
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
            true, true, true, true, false,
            "수집",
            "red",
            List.of("INVALID_CATEGORY", "FOOD_SNACK")
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);
        given(userAgreementRepository.existsByUser(spyUser)).willReturn(false);

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
            true, true, true, true, false,
            "수집",
            "red",
            List.of("FOOD_SNACK")
        );

        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.completeOnboarding(999L, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("회원탈퇴 시 User는 익명화되고 약관 동의도 삭제된다.")
    void withdraw_success() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "테스트유저", "테스트", null, null);
        User spyUser = spy(user);

        RefreshToken refreshToken = RefreshToken.of(
            spyUser,
            "refresh_token",
            LocalDateTime.now().plusDays(30)
        );

        UserAgreement agreement = UserAgreement.of(
            spyUser,
            true, true, true, true, false
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(refreshTokenRepository.findByUser(spyUser)).willReturn(Optional.of(refreshToken));
        given(userAgreementRepository.findByUser(spyUser)).willReturn(Optional.of(agreement));

        // when
        userService.withdraw(1L);

        // then
        verify(spyUser).anonymize();
        verify(refreshTokenRepository).delete(refreshToken);
        verify(userAgreementRepository).delete(agreement);
        verify(userRepository).delete(spyUser);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 회원탈퇴 시 에러가 발생한다.")
    void withdraw_withNotExistUser_throwsException() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.withdraw(999L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("Refresh Token과 약관 동의가 없어도 회원탈퇴는 성공한다.")
    void withdraw_withoutRefreshTokenAndAgreement_success() {
        // given
        User user = User.of(Provider.KAKAO, "kakao123", "테스트유저", "테스트", null, null);
        User spyUser = spy(user);

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(refreshTokenRepository.findByUser(spyUser)).willReturn(Optional.empty());
        given(userAgreementRepository.findByUser(spyUser)).willReturn(Optional.empty());

        // when
        userService.withdraw(1L);

        // then
        verify(spyUser).anonymize();
        verify(userRepository).delete(spyUser);
        verify(refreshTokenRepository, never()).delete(any());
        verify(userAgreementRepository, never()).delete(any());
    }

    @Test
    @DisplayName("탈퇴 후 30일 지난 유저 삭제 쿼리를 호출하고 삭제 결과를 반환한다")
    void deleteWithdrawnUsers_callsRepositoryAndReturnsCount() {
        // given
        given(userRepository.deleteByDeletedTrueAndDeletedAtBefore(any(LocalDateTime.class)))
                .willReturn(3L);

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);

        // when
        long result = userService.deleteWithdrawnUsers();

        // then
        verify(userRepository).deleteByDeletedTrueAndDeletedAtBefore(captor.capture());

        LocalDateTime cutoff = captor.getValue();

        assertThat(cutoff)
                .isBeforeOrEqualTo(LocalDateTime.now().minusDays(30).plusSeconds(1))
                .isAfterOrEqualTo(LocalDateTime.now().minusDays(30).minusSeconds(1));

        assertThat(result).isEqualTo(3L);
    }
}
