package com.souzip.domain.wishlist.service;

import com.souzip.application.file.FileQueryService;
import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.file.required.FileStorage;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.file.File;
import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.souvenir.repository.SouvenirRepository;
import com.souzip.domain.user.entity.User;
import com.souzip.domain.user.repository.UserRepository;
import com.souzip.domain.wishlist.dto.MyWishlistListResponse;
import com.souzip.domain.wishlist.dto.MyWishlistResponse;
import com.souzip.domain.wishlist.dto.WishlistResponse;
import com.souzip.domain.wishlist.entity.Wishlist;
import com.souzip.domain.wishlist.repository.WishlistRepository;
import com.souzip.global.exception.BusinessException;
import com.souzip.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final SouvenirRepository souvenirRepository;
    private final FileQueryService fileQueryService;
    private final FileStorage fileStorage;

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

    public MyWishlistListResponse getMyWishlist(Long userId, Pageable pageable) {
        Page<Wishlist> wishlistPage = wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<Long> souvenirIds = wishlistPage.getContent().stream()
                .map(w -> w.getSouvenir().getId())
                .toList();

        Map<Long, FileResponse> thumbnailMap = getThumbnails(souvenirIds);

        Page<MyWishlistResponse> responsePage = wishlistPage.map(wishlist -> {
            String thumbnailUrl = Optional.ofNullable(thumbnailMap.get(wishlist.getSouvenir().getId()))
                    .map(FileResponse::url)
                    .orElse(null);
            return MyWishlistResponse.of(wishlist, thumbnailUrl);
        });

        return MyWishlistListResponse.from(responsePage);
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

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Souvenir findSouvenirById(Long souvenirId) {
        return souvenirRepository.findByIdAndDeletedFalse(souvenirId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOUVENIR_NOT_FOUND));
    }
}
