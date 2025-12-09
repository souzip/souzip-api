package com.souzip.api.domain.file.entity;

import com.souzip.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
    name = "file",
    indexes = {
        @Index(name = "idx_file_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_file_entity_order", columnList = "entity_type, entity_id, display_order"),
        @Index(name = "idx_file_created_at", columnList = "created_at"),
        @Index(name = "idx_file_storage_key", columnList = "storage_key")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_file_entity_order",
            columnNames = {"entity_type", "entity_id", "display_order"}
        )
    }
)
@Entity
public class File extends BaseEntity {

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 1;

    public static File of(
        String entityType,
        Long entityId,
        String storageKey,
        String originalName,
        Long fileSize,
        String type,
        Integer displayOrder
    ) {
        return File.builder()
            .entityType(entityType)
            .entityId(entityId)
            .storageKey(storageKey)
            .originalName(originalName)
            .fileSize(fileSize)
            .type(type)
            .displayOrder(displayOrder)
            .build();
    }

    public void updateDisplayOrder(Integer newOrder) {
        this.displayOrder = newOrder;
    }
}
