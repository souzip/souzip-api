package com.souzip.api.domain.user.service;

import com.souzip.api.domain.auth.repository.RefreshTokenRepository;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.domain.user.repository.UserRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
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
    public void withdraw(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.anonymize();

        refreshTokenRepository.findByUser(user)
            .ifPresent(refreshTokenRepository::delete);

        userRepository.delete(user);
    }
}
