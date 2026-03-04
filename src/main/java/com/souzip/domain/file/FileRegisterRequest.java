package com.souzip.domain.file;

public record FileRegisterRequest(
        EntityType entityType,
        Long entityId,
        String storageKey,
        String originalName,
        Long fileSize,
        String contentType,
        Integer displayOrder
) {
    public static FileRegisterRequest of(
            EntityType entityType,
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
