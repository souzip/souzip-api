package com.souzip.application.file;

import com.souzip.domain.file.EntityType;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(Long fileId) {
        super("파일을 찾을 수 없습니다. id: " + fileId);
    }

    public FileNotFoundException(EntityType entityType, Long entityId) {
        super(String.format("파일을 찾을 수 없습니다. entityType: %s, entityId: %d",
                entityType, entityId));
    }
}
