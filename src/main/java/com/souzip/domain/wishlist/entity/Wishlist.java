package com.souzip.domain.wishlist.entity;

import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.user.entity.User;
import com.souzip.shared.domain.BaseEntity;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
public class Wishlist extends BaseEntity {

    private User user;

    private Souvenir souvenir;

    public static Wishlist of(User user, Souvenir souvenir) {
        return Wishlist.builder()
                .user(user)
                .souvenir(souvenir)
                .build();
    }
}
