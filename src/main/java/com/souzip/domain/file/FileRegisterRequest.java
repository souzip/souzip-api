package com.souzip.domain.file;

public record FileRegisterRequest(
        String entityType,
        Long entityId,
        String storageKey,
        String originalName,
        Long fileSize,
        String contentType,
        Integer displayOrder
) {
    public static FileRegisterRequest of(
            String entityType,
            Long entityId,
            String storageKey,
            String originalName,
            Long fileSize,
            String contentType,
            Integer displayOrder
    ) {
        return new FileRegisterRequest(
                entityType, entityId, storageKey,
                originalName, fileSize, contentType, displayOrder
        );
    }
}
