package com.souzip.domain.wishlist.entity;

import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WishlistTest {

    @Test
    @DisplayName("of 호출 시 user와 souvenir가 올바르게 설정된다.")
    void of_success() {
        // given
        User user = mock(User.class);
        Souvenir souvenir = mock(Souvenir.class);

        // when
        Wishlist wishlist = Wishlist.of(user, souvenir);

        // then
        assertThat(wishlist.getUser()).isEqualTo(user);
        assertThat(wishlist.getSouvenir()).isEqualTo(souvenir);
    }
}
