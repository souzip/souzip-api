package com.souzip.domain.wishlist.service;

import com.souzip.application.file.FileQueryService;
import com.souzip.application.file.required.FileStorage;
import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.souvenir.repository.SouvenirRepository;
import com.souzip.domain.user.entity.User;
import com.souzip.domain.user.repository.UserRepository;
import com.souzip.domain.wishlist.dto.WishlistResponse;
import com.souzip.domain.wishlist.entity.Wishlist;
import com.souzip.domain.wishlist.repository.WishlistRepository;
import com.souzip.global.exception.BusinessException;
import com.souzip.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final SouvenirRepository souvenirRepository;

    @Transactional
    public WishlistResponse addWishlist(Long userId, Long souvenirId) {
        User user = findUserById(userId);
        Souvenir souvenir = findSouvenirById(souvenirId);
        try {
            wishlistRepository.save(Wishlist.of(user, souvenir));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ALREADY_WISHLISTED);
        }
        return WishlistResponse.of(souvenirId, true);
    }

    @Transactional
    public WishlistResponse removeWishlist(Long userId, Long souvenirId) {
        if (!wishlistRepository.existsByUserIdAndSouvenirId(userId, souvenirId)) {
            throw new BusinessException(ErrorCode.WISHLIST_NOT_FOUND);
        }
        wishlistRepository.deleteByUserIdAndSouvenirId(userId, souvenirId);
        return WishlistResponse.of(souvenirId, false);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Souvenir findSouvenirById(Long souvenirId) {
        return souvenirRepository.findByIdAndDeletedFalse(souvenirId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOUVENIR_NOT_FOUND));
    }
}
