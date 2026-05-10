package com.souzip.domain.wishlist.service;

import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.souvenir.repository.SouvenirRepository;
import com.souzip.domain.user.entity.User;
import com.souzip.domain.user.repository.UserRepository;
import com.souzip.domain.wishlist.dto.WishlistResponse;
import com.souzip.domain.wishlist.entity.Wishlist;
import com.souzip.domain.wishlist.repository.WishlistRepository;
import com.souzip.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SouvenirRepository souvenirRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    @DisplayName("찜 등록에 성공한다.")
    void addWishlist_success() {
        // given
        Long userId = 1L;
        Long souvenirId = 1L;

        User user = mock(User.class);
        Souvenir souvenir = mock(Souvenir.class);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(souvenirRepository.findByIdAndDeletedFalse(souvenirId)).willReturn(Optional.of(souvenir));
        given(wishlistRepository.save(any(Wishlist.class))).willReturn(any());

        // when
        WishlistResponse response = wishlistService.addWishlist(userId, souvenirId);

        // then
        assertThat(response.souvenirId()).isEqualTo(souvenirId);
        assertThat(response.wishlisted()).isTrue();
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("이미 찜한 기념품을 다시 찜하면 에러가 발생한다.")
    void addWishlist_alreadyWishlisted() {
        // given
        Long userId = 1L;
        Long souvenirId = 1L;

        User user = mock(User.class);
        Souvenir souvenir = mock(Souvenir.class);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(souvenirRepository.findByIdAndDeletedFalse(souvenirId)).willReturn(Optional.of(souvenir));
        given(wishlistRepository.save(any(Wishlist.class))).willThrow(DataIntegrityViolationException.class);

        // when & then
        assertThatThrownBy(() -> wishlistService.addWishlist(userId, souvenirId))
                .isInstanceOf(BusinessException.class);

        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("존재하지 않는 기념품을 찜하면 에러가 발생한다.")
    void addWishlist_souvenirNotFound() {
        // given
        Long userId = 1L;
        Long souvenirId = 999L;

        User user = mock(User.class);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(souvenirRepository.findByIdAndDeletedFalse(souvenirId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wishlistService.addWishlist(userId, souvenirId))
                .isInstanceOf(BusinessException.class);

        verify(wishlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("찜 취소에 성공한다.")
    void removeWishlist_success() {
        // given
        Long userId = 1L;
        Long souvenirId = 1L;
        String userUuid = "1ffc89fd-bb85-489e-9218-63c4f9680b27";

        User user = mock(User.class);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(user.getUserId()).willReturn(userUuid);
        given(wishlistRepository.existsByUserUserIdAndSouvenirId(userUuid, souvenirId)).willReturn(true);

        // when
        WishlistResponse response = wishlistService.removeWishlist(userId, souvenirId);

        // then
        assertThat(response.souvenirId()).isEqualTo(souvenirId);
        assertThat(response.wishlisted()).isFalse();
        verify(wishlistRepository).deleteByUserIdAndSouvenirId(userId, souvenirId);
    }

    @Test
    @DisplayName("찜하지 않은 기념품을 취소하면 에러가 발생한다.")
    void removeWishlist_notFound() {
        // given
        Long userId = 1L;
        Long souvenirId = 1L;
        String userUuid = "1ffc89fd-bb85-489e-9218-63c4f9680b27";

        User user = mock(User.class);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(user.getUserId()).willReturn(userUuid);
        given(wishlistRepository.existsByUserUserIdAndSouvenirId(userUuid, souvenirId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> wishlistService.removeWishlist(userId, souvenirId))
                .isInstanceOf(BusinessException.class);

        verify(wishlistRepository, never()).deleteByUserIdAndSouvenirId(any(), any());
    }
}
