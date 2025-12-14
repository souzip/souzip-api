package com.souzip.api.domain.user.service;

import com.souzip.api.domain.auth.repository.RefreshTokenRepository;
import com.souzip.api.domain.category.dto.CategoryDto;
import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.user.dto.OnboardingRequest;
import com.souzip.api.domain.user.dto.OnboardingResponse;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.domain.user.repository.UserRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public OnboardingResponse completeOnboarding(Long userId, OnboardingRequest request) {
        User user = findUserById(userId);
        validateOnboardingNotCompleted(user);

        Set<Category> categories = convertToCategories(request.categories());
        user.completeOnboarding(request.nickname(), request.profileImageUrl(), categories);

        List<CategoryDto> categoryDto = convertToCategoryDto(categories);
        return OnboardingResponse.of(user, categoryDto);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = findUserById(userId);
        deleteRefreshTokenIfExists(user);

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
}
