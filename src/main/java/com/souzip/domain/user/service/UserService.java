package com.souzip.domain.user.service;

import com.souzip.application.file.FileQueryService;
import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.file.required.FileStorage;
import com.souzip.domain.audit.entity.AuditAction;
import com.souzip.domain.auth.repository.RefreshTokenRepository;
import com.souzip.domain.category.dto.CategoryDto;
import com.souzip.domain.category.entity.Category;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.file.File;
import com.souzip.domain.souvenir.dto.MySouvenirListResponse;
import com.souzip.domain.souvenir.dto.MySouvenirResponse;
import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.souvenir.repository.SouvenirRepository;
import com.souzip.domain.user.dto.NicknameCheckResponse;
import com.souzip.domain.user.dto.OnboardingRequest;
import com.souzip.domain.user.dto.OnboardingResponse;
import com.souzip.domain.user.dto.UserProfileResponse;
import com.souzip.domain.user.entity.User;
import com.souzip.domain.user.entity.UserAgreement;
import com.souzip.domain.user.repository.UserAgreementRepository;
import com.souzip.domain.user.repository.UserRepository;
import com.souzip.domain.wishlist.repository.WishlistRepository;
import com.souzip.global.audit.annotation.Audit;
import com.souzip.global.exception.BusinessException;
import com.souzip.global.exception.ErrorCode;

import java.time.LocalDateTime;
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
    private final WishlistRepository wishlistRepository;
    private final FileQueryService fileQueryService;
    private final FileStorage fileStorage;

    public NicknameCheckResponse checkNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            return NicknameCheckResponse.unavailable();
        }

        return NicknameCheckResponse.available();
    }

    @Audit(action = AuditAction.ONBOARDING_AGREEMENTS)
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

    @Audit(action = AuditAction.WITHDRAW)
    @Transactional
    public void withdraw(Long userId) {
        User user = findUserById(userId);
        deleteRefreshTokenIfExists(user);
        deleteUserAgreementIfExists(user);
        user.anonymize();
        userRepository.delete(user);
    }

    @Transactional
    public long deleteWithdrawnUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        return userRepository.deleteByDeletedTrueAndDeletedAtBefore(cutoff);
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

        Map<Long, FileResponse> thumbnailMap = getThumbnails(souvenirIds);
        Set<Long> wishlistedIds = wishlistRepository.findSouvenirIdsByUserId(user.getUserId());
        Map<Long, Long> wishlistCountMap = wishlistRepository.countBySouvenirIds(souvenirIds);

        Page<MySouvenirResponse> responsePage = souvenirPage.map(souvenir -> {
            String thumbnailUrl = Optional.ofNullable(thumbnailMap.get(souvenir.getId()))
                    .map(FileResponse::url)
                    .orElse(null);
            boolean isWishlisted = wishlistedIds.contains(souvenir.getId());
            long wishlistCount = wishlistCountMap.getOrDefault(souvenir.getId(), 0L);
            return MySouvenirResponse.of(souvenir, thumbnailUrl, isWishlisted, wishlistCount);
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

    private Map<Long, FileResponse> getThumbnails(List<Long> souvenirIds) {
        Map<Long, File> fileMap = fileQueryService.findThumbnailsByEntityIds(EntityType.SOUVENIR, souvenirIds);

        return fileMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> FileResponse.of(
                                entry.getValue(),
                                fileStorage.generateUrl(entry.getValue().getStorageKey())
                        )
                ));
    }
}
