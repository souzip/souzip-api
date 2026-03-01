package com.souzip.api.domain.file;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(Long fileId) {
        super("파일을 찾을 수 없습니다. id: " + fileId);
    }

    public FileNotFoundException(String entityType, Long entityId) {
        super(String.format("파일을 찾을 수 없습니다. entityType: %s, entityId: %d",
                entityType, entityId));
    }
}
