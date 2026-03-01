package com.souzip.api.domain.file;

import com.souzip.api.domain.shared.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File extends BaseEntity {

    private String entityType;

    private Long entityId;

    private String storageKey;

    private String originalName;

    private Long fileSize;

    private String contentType;

    private Integer displayOrder;

    public static File create(
            String entityType,
            Long entityId,
            String storageKey,
            String originalName,
            Long fileSize,
            String contentType,
            Integer displayOrder
    ) {
        File file = new File();

        file.entityType = requireNonNull(entityType, "엔티티 타입은 필수입니다.");
        file.entityId = requireNonNull(entityId, "엔티티 ID는 필수입니다.");
        file.storageKey = requireNonNull(storageKey, "스토리지 키는 필수입니다.");
        file.originalName = requireNonNull(originalName, "파일명은 필수입니다.");
        file.fileSize = requireNonNull(fileSize, "파일 크기는 필수입니다.");
        file.contentType = requireNonNull(contentType, "파일 타입은 필수입니다.");
        file.displayOrder = requireNonNull(displayOrder, "정렬 순서는 필수입니다.");

        return file;
    }

    public void updateDisplayOrder(Integer newOrder) {
        this.displayOrder = requireNonNull(newOrder, "정렬 순서는 필수입니다.");
    }
}
