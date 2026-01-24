package com.souzip.api.domain.user.service;

import com.souzip.api.domain.auth.repository.RefreshTokenRepository;
import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.file.service.FileService;
import com.souzip.api.domain.souvenir.dto.MySouvenirListResponse;
import com.souzip.api.domain.souvenir.dto.MySouvenirResponse;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import com.souzip.api.domain.souvenir.repository.SouvenirRepository;
import com.souzip.api.domain.user.dto.NicknameCheckResponse;
import com.souzip.api.domain.user.dto.OnboardingRequest;
import com.souzip.api.domain.user.dto.OnboardingResponse;
import com.souzip.api.domain.user.dto.UserProfileResponse;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.domain.user.entity.UserAgreement;
import com.souzip.api.domain.user.repository.UserAgreementRepository;
import com.souzip.api.domain.user.repository.UserRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final SouvenirRepository souvenirRepository;
    private final FileService fileService;

    public NicknameCheckResponse checkNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            return NicknameCheckResponse.unavailable();
        }

        return NicknameCheckResponse.available();
    }

    @Transactional
    public OnboardingResponse completeOnboarding(Long userId, OnboardingRequest request) {
        User user = findUserById(userId);

        validateOnboardingNotCompleted(user);
        validateRequiredAgreements(request);
        validateNicknameNotDuplicated(request.nickname());

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

    public UserProfileResponse getUserProfile(Long userId) {
        User user = findUserById(userId);
        return UserProfileResponse.from(user);
    }

    public MySouvenirListResponse getMySouvenirs(Long userId, int page, int size) {
        User user = findUserById(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Souvenir> souvenirPage = souvenirRepository.findByUserWithUser(user, pageable);

        List<Long> souvenirIds = souvenirPage.getContent().stream()
            .map(Souvenir::getId)
            .toList();

        Map<Long, FileResponse> thumbnailMap = fileService
            .getThumbnailsByEntityIds("Souvenir", souvenirIds);

        Page<MySouvenirResponse> responsePage = souvenirPage.map(souvenir -> {
            String thumbnailUrl = Optional.ofNullable(thumbnailMap.get(souvenir.getId()))
                .map(FileResponse::url)
                .orElse(null);
            return MySouvenirResponse.of(souvenir, thumbnailUrl);
        });

        return MySouvenirListResponse.from(responsePage);
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
        if (isRequiredAgreementNotAccepted(request)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "필수 약관에 모두 동의해야 합니다.");
        }
    }

    private UserAgreement saveUserAgreement(User user, OnboardingRequest request) {
        if (userAgreementRepository.existsByUser(user)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 약관에 동의한 사용자입니다.");
        }

        UserAgreement agreement = UserAgreement.of(user, request);
        return userAgreementRepository.save(agreement);
    }

    private void validateNicknameNotDuplicated(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
        }
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

    private boolean isRequiredAgreementNotAccepted(OnboardingRequest request) {
        return !request.ageVerified()
            || !request.serviceTerms()
            || !request.privacyRequired()
            || !request.locationService();
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
