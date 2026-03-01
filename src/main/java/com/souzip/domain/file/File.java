package com.souzip.domain.file;

import com.souzip.domain.shared.BaseEntity;
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

    private String type;

    private Integer displayOrder;

    public static File register(FileRegisterRequest request) {
        File file = new File();

        file.entityType = requireNonNull(request.entityType(), "엔티티 타입은 필수입니다.");
        file.entityId = requireNonNull(request.entityId(), "엔티티 ID는 필수입니다.");
        file.storageKey = requireNonNull(request.storageKey(), "스토리지 키는 필수입니다.");
        file.originalName = requireNonNull(request.originalName(), "파일명은 필수입니다.");
        file.fileSize = requireNonNull(request.fileSize(), "파일 크기는 필수입니다.");
        file.type = requireNonNull(request.type(), "파일 타입은 필수입니다.");
        file.displayOrder = requireNonNull(request.displayOrder(), "정렬 순서는 필수입니다.");

        return file;
    }
}
