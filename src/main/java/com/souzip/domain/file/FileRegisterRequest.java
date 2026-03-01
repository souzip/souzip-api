package com.souzip.domain.file;

public record FileRegisterRequest(
        String entityType,
        Long entityId,
        String storageKey,
        String originalName,
        Long fileSize,
        String type,
        Integer displayOrder
) {
    public static FileRegisterRequest of(
            String entityType,
            Long entityId,
            String storageKey,
            String originalName,
            Long fileSize,
            String type,
            Integer displayOrder
    ) {
        return new FileRegisterRequest(
                entityType, entityId, storageKey,
                originalName, fileSize, type, displayOrder
        );
    }
}
