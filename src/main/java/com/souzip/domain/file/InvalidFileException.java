package com.souzip.domain.file;

public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }

    public static InvalidFileException empty() {
        return new InvalidFileException("파일이 비어있습니다.");
    }

    public static InvalidFileException sizeExceeded(long size, long maxSize) {
        return new InvalidFileException(
                String.format("파일 크기가 초과되었습니다. (현재: %dMB, 최대: %dMB)",
                        size / 1024 / 1024, maxSize / 1024 / 1024)
        );
    }

    public static InvalidFileException invalidType(String filename) {
        return new InvalidFileException(
                String.format("허용되지 않은 파일 형식입니다. 파일: %s", filename)
        );
    }
}
