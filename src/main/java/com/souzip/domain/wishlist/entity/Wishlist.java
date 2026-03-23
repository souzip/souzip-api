package com.souzip.domain.wishlist.entity;

import com.souzip.domain.shared.BaseEntity;
import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "wishlist", indexes = {
    @Index(name = "idx_wishlist_user_souvenir", columnList = "user_id, souvenir_id", unique = true),
    @Index(name = "idx_wishlist_user_created", columnList = "user_id, created_at")
})
@Entity
public class Wishlist extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "souvenir_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Souvenir souvenir;

    public static Wishlist of(User user, Souvenir souvenir) {
        return Wishlist.builder()
                .user(user)
                .souvenir(souvenir)
                .build();
    }
}
