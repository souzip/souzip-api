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

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public OnboardingResponse completeOnboarding(Long userId, OnboardingRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.needsOnboarding()) {
            throw new BusinessException(ErrorCode.ONBOARDING_ALREADY_COMPLETED);
        }

        Set<Category> categories = new HashSet<>();
        for (String categoryName : request.categories()) {
            Optional<Category> category = Category.from(categoryName);
            if (category.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_CATEGORY);
            }
            categories.add(category.get());
        }

        user.completeOnboarding(request.nickname(), request.profileImageUrl(), categories);

        List<CategoryDto> categoryDto = categories.stream()
            .map(CategoryDto::from)
            .toList();

        return OnboardingResponse.of(user, categoryDto);
    }

    @Transactional
    public void withdraw(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.anonymize();

        refreshTokenRepository.findByUser(user)
            .ifPresent(refreshTokenRepository::delete);

        userRepository.delete(user);
    }
}
