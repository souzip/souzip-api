package com.souzip.domain.user.service;

import com.souzip.auth.application.required.RefreshTokenRepository;
import com.souzip.domain.shared.Provider;
import com.souzip.domain.user.dto.NicknameCheckResponse;
import com.souzip.domain.user.dto.OnboardingRequest;
import com.souzip.domain.user.dto.OnboardingResponse;
import com.souzip.domain.user.entity.User;
import com.souzip.domain.user.entity.UserAgreement;
import com.souzip.domain.user.repository.UserAgreementRepository;
import com.souzip.domain.user.repository.UserRepository;
import com.souzip.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
        given(userRepository.existsByNickname("새로운닉네임")).willReturn(false);

        NicknameCheckResponse response = userService.checkNickname("새로운닉네임");

        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getMessage()).isEqualTo("사용 가능한 닉네임입니다.");
        verify(userRepository).existsByNickname("새로운닉네임");
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임을 확인한다.")
    void checkNickname_unavailable() {
        given(userRepository.existsByNickname("중복닉네임")).willReturn(true);

        NicknameCheckResponse response = userService.checkNickname("중복닉네임");

        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getMessage()).isEqualTo("이미 사용 중인 닉네임입니다.");
    }

    @Test
    @DisplayName("온보딩 시 닉네임이 중복되면 에러가 발생한다.")
    void completeOnboarding_nicknameAlreadyExists() {
        User user = User.of(Provider.KAKAO, "kakao123", "test@kakao.com");
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
                true, true, true, true, false,
                "중복닉네임", "red", List.of("FOOD_SNACK")
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);
        given(userRepository.existsByNickname("중복닉네임")).willReturn(true);

        assertThatThrownBy(() -> userService.completeOnboarding(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 사용 중인 닉네임입니다.");

        verify(userAgreementRepository, never()).save(any());
        verify(spyUser, never()).completeOnboarding(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("온보딩을 완료하면 User와 UserAgreement가 저장된다.")
    void completeOnboarding_success() {
        User user = User.of(Provider.KAKAO, "kakao123", "test@kakao.com");
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
                true, true, true, true, false,
                "수집", "red",
                List.of("FOOD_SNACK", "BEAUTY_HEALTH", "FASHION_ACCESSORY")
        );

        String expectedImageUrl = "https://kr.object.ncloudstorage.com/souzip-dev-images/profile/red.svg";

        UserAgreement agreement = UserAgreement.of(spyUser, true, true, true, true, false);

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);
        given(userAgreementRepository.existsByUser(spyUser)).willReturn(false);
        given(userAgreementRepository.save(any(UserAgreement.class))).willReturn(agreement);
        given(profileImageService.resolveProfileImageUrl("red")).willReturn(expectedImageUrl);
        given(spyUser.getUserId()).willReturn("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        given(spyUser.getNickname()).willReturn("수집");
        given(spyUser.getProfileImageUrl()).willReturn(expectedImageUrl);

        OnboardingResponse response = userService.completeOnboarding(1L, request);

        assertThat(response.userId()).isEqualTo("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        assertThat(response.nickname()).isEqualTo("수집");
        assertThat(response.profileImageUrl()).isEqualTo(expectedImageUrl);
        assertThat(response.categories()).hasSize(3);
        assertThat(response.agreements().ageVerified()).isTrue();
        assertThat(response.agreements().marketingConsent()).isFalse();

        verify(userAgreementRepository).save(any(UserAgreement.class));
        verify(profileImageService).resolveProfileImageUrl("red");
        verify(spyUser).completeOnboarding(eq("수집"), eq(expectedImageUrl), any());
    }

    @Test
    @DisplayName("필수 약관에 동의하지 않으면 온보딩에 실패한다.")
    void completeOnboarding_requiredAgreementNotChecked() {
        User user = User.of(Provider.KAKAO, "kakao123", "test@kakao.com");
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
                true, false, true, true, false,
                "수집", "red", List.of("FOOD_SNACK")
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);

        assertThatThrownBy(() -> userService.completeOnboarding(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("필수 약관에 모두 동의해야 합니다.");

        verify(userAgreementRepository, never()).save(any());
        verify(spyUser, never()).completeOnboarding(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("이미 약관에 동의한 사용자는 중복 저장되지 않는다.")
    void completeOnboarding_agreementAlreadyExists() {
        User user = User.of(Provider.KAKAO, "kakao123", "test@kakao.com");
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
                true, true, true, true, false,
                "수집", "red", List.of("FOOD_SNACK")
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);
        given(userAgreementRepository.existsByUser(spyUser)).willReturn(true);

        assertThatThrownBy(() -> userService.completeOnboarding(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 약관에 동의한 사용자입니다.");

        verify(userAgreementRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 온보딩을 완료한 사용자는 다시 온보딩할 수 없다.")
    void completeOnboarding_alreadyCompleted() {
        User user = User.of(Provider.KAKAO, "kakao123", "test@kakao.com");
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
                true, true, true, true, false,
                "새닉네임", "blue", List.of("FOOD_SNACK")
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(false);

        assertThatThrownBy(() -> userService.completeOnboarding(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 온보딩을 완료한 사용자입니다.");

        verify(profileImageService, never()).resolveProfileImageUrl(anyString());
        verify(spyUser, never()).completeOnboarding(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("유효하지 않은 카테고리로 온보딩 시 에러가 발생한다.")
    void completeOnboarding_invalidCategory() {
        User user = User.of(Provider.KAKAO, "kakao123", "test@kakao.com");
        User spyUser = spy(user);

        OnboardingRequest request = new OnboardingRequest(
                true, true, true, true, false,
                "수집", "red", List.of("INVALID_CATEGORY", "FOOD_SNACK")
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(spyUser.needsOnboarding()).willReturn(true);
        given(userAgreementRepository.existsByUser(spyUser)).willReturn(false);

        assertThatThrownBy(() -> userService.completeOnboarding(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("유효하지 않은 카테고리입니다.");

        verify(spyUser, never()).completeOnboarding(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 온보딩 시 에러가 발생한다.")
    void completeOnboarding_userNotFound() {
        OnboardingRequest request = new OnboardingRequest(
                true, true, true, true, false,
                "수집", "red", List.of("FOOD_SNACK")
        );

        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.completeOnboarding(999L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("회원탈퇴 시 User는 익명화되고 약관 동의도 삭제된다.")
    void withdraw_success() {
        User user = User.of(Provider.KAKAO, "kakao123", "test@kakao.com");
        User spyUser = spy(user);

        UserAgreement agreement = UserAgreement.of(spyUser, true, true, true, true, false);

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(userAgreementRepository.findByUser(spyUser)).willReturn(Optional.of(agreement));

        userService.withdraw(1L);

        verify(spyUser).anonymize();
        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(userAgreementRepository).delete(agreement);
        verify(userRepository).delete(spyUser);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 회원탈퇴 시 에러가 발생한다.")
    void withdraw_withNotExistUser_throwsException() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.withdraw(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("약관 동의가 없어도 회원탈퇴는 성공한다.")
    void withdraw_withoutAgreement_success() {
        User user = User.of(Provider.KAKAO, "kakao123", "test@kakao.com");
        User spyUser = spy(user);

        given(userRepository.findById(1L)).willReturn(Optional.of(spyUser));
        given(userAgreementRepository.findByUser(spyUser)).willReturn(Optional.empty());

        userService.withdraw(1L);

        verify(spyUser).anonymize();
        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(userRepository).delete(spyUser);
        verify(userAgreementRepository, never()).delete(any());
    }

    @Test
    @DisplayName("탈퇴된 유저 삭제 쿼리를 호출하고 삭제 결과를 반환한다")
    void deleteWithdrawnUsers_callsRepositoryAndReturnsCount() {
<<<<<<< Updated upstream
        // given
        given(userRepository.deleteByDeletedTrue())
                .willReturn(3L);

        // when
        long result = userService.deleteWithdrawnUsers();

        // then
        verify(userRepository).deleteByDeletedTrue();
=======
        given(userRepository.deleteByDeletedTrueAndDeletedAtBefore(any(LocalDateTime.class)))
                .willReturn(3L);

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);

        long result = userService.deleteWithdrawnUsers();

        verify(userRepository).deleteByDeletedTrueAndDeletedAtBefore(captor.capture());

        LocalDateTime cutoff = captor.getValue();
        assertThat(cutoff)
                .isBeforeOrEqualTo(LocalDateTime.now().minusDays(30).plusSeconds(1))
                .isAfterOrEqualTo(LocalDateTime.now().minusDays(30).minusSeconds(1));

>>>>>>> Stashed changes
        assertThat(result).isEqualTo(3L);
    }
}