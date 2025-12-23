package com.souzip.api.domain.user.service;

import com.souzip.api.domain.auth.repository.RefreshTokenRepository;
import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.user.dto.OnboardingRequest;
import com.souzip.api.domain.user.dto.OnboardingResponse;
import com.souzip.api.domain.user.dto.ProfileColorsResponse;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.domain.user.entity.UserAgreement;
import com.souzip.api.domain.user.repository.UserAgreementRepository;
import com.souzip.api.domain.user.repository.UserRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserAgreementRepository userAgreementRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ProfileImageService profileImageService;

    @Transactional
    public OnboardingResponse completeOnboarding(Long userId, OnboardingRequest request) {
        User user = findUserById(userId);
        validateOnboardingNotCompleted(user);

        validateRequiredAgreements(request);

        UserAgreement agreement = saveUserAgreement(user, request);

        String profileImageUrl = profileImageService.resolveProfileImageUrl(
            request.profileImageColor()
        );
        Set<Category> categories = convertToCategories(request.categories());
        user.completeOnboarding(request.nickname(), profileImageUrl, categories);

        List<CategoryDto> categoryDto = convertToCategoryDto(categories);
        return OnboardingResponse.of(user, categoryDto, agreement);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = findUserById(userId);
        deleteRefreshTokenIfExists(user);
        deleteUserAgreementIfExists(user);
        user.anonymize();
        userRepository.delete(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateOnboardingNotCompleted(User user) {
        if (!user.needsOnboarding()) {
            throw new BusinessException(ErrorCode.ONBOARDING_ALREADY_COMPLETED);
        }
    }

    private void validateRequiredAgreements(OnboardingRequest request) {
        if (!request.ageVerified() || !request.serviceTerms()
            || !request.privacyRequired() || !request.locationService()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "필수 약관에 모두 동의해야 합니다.");
        }
    }

    private UserAgreement saveUserAgreement(User user, OnboardingRequest request) {
        // 이미 약관 동의가 있는지 확인
        if (userAgreementRepository.existsByUser(user)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 약관에 동의한 사용자입니다.");
        }

        UserAgreement agreement = UserAgreement.of(
            user,
            request.ageVerified(),
            request.serviceTerms(),
            request.privacyRequired(),
            request.locationService(),
            request.marketingConsent()
        );

        return userAgreementRepository.save(agreement);
    }

    private Set<Category> convertToCategories(List<String> categoryNames) {
        return categoryNames.stream()
            .map(this::convertToCategory)
            .collect(Collectors.toSet());
    }

    private Category convertToCategory(String categoryName) {
        return Category.from(categoryName)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CATEGORY));
    }

    private List<CategoryDto> convertToCategoryDto(Set<Category> categories) {
        return categories.stream()
            .map(CategoryDto::from)
            .toList();
    }

    private void deleteRefreshTokenIfExists(User user) {
        refreshTokenRepository.findByUser(user)
            .ifPresent(refreshTokenRepository::delete);
    }

    private void deleteUserAgreementIfExists(User user) {
        userAgreementRepository.findByUser(user)
            .ifPresent(userAgreementRepository::delete);
    }
}
