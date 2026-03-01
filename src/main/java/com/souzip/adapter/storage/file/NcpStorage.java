package com.souzip.adapter.storage.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.souzip.adapter.config.ObjectStorageProperties;
import com.souzip.application.file.required.FileStorage;
import com.souzip.domain.file.InvalidFileException;
import com.souzip.global.exception.BusinessException;
import com.souzip.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class NcpStorage implements FileStorage {

    private final AmazonS3 s3Client;
    private final ObjectStorageProperties properties;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    private static final int URL_EXPIRATION_MS = 3600000;

    @Override
    public String upload(String userId, MultipartFile file) {
        validateFile(file);
        String storageKey = generateStorageKey(userId, file.getOriginalFilename());

        try {
            uploadToNcp(file, storageKey);
            log.info("NCP 업로드 완료: {}", storageKey);
            return storageKey;
        } catch (IOException e) {
            log.error("NCP 업로드 실패: {}", storageKey, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            s3Client.deleteObject(properties.getBucket(), storageKey);
            log.info("NCP 삭제 완료: {}", storageKey);
        } catch (Exception e) {
            log.error("NCP 삭제 실패: {}", storageKey, e);
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    @Override
    public String generateUrl(String storageKey) {
        Date expiration = new Date(System.currentTimeMillis() + URL_EXPIRATION_MS);
        return s3Client.generatePresignedUrl(properties.getBucket(), storageKey, expiration).toString();
    }

    private void uploadToNcp(MultipartFile file, String storageKey) throws IOException {
        ObjectMetadata metadata = createMetadata(file);
        s3Client.putObject(
                properties.getBucket(),
                storageKey,
                file.getInputStream(),
                metadata
        );
    }

    private ObjectMetadata createMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        metadata.setCacheControl("max-age=31536000");
        return metadata;
    }

    private void validateFile(MultipartFile file) {
        validateFileNotEmpty(file);
        validateFileSize(file);
        validateFileType(file);
    }

    private void validateFileNotEmpty(MultipartFile file) {
        if (isEmpty(file)) {
            throw InvalidFileException.empty();
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (exceedsMaxSize(file)) {
            throw InvalidFileException.sizeExceeded(file.getSize(), MAX_FILE_SIZE);
        }
    }

    private void validateFileType(MultipartFile file) {
        if (!hasAllowedExtension(file)) {
            throw InvalidFileException.invalidType(file.getOriginalFilename());
        }
    }

    private boolean isEmpty(MultipartFile file) {
        return file == null || file.isEmpty();
    }

    private boolean exceedsMaxSize(MultipartFile file) {
        return file.getSize() > MAX_FILE_SIZE;
    }

    private boolean hasAllowedExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }

        String lowerFilename = filename.toLowerCase();
        return ALLOWED_EXTENSIONS.stream()
                .anyMatch(lowerFilename::endsWith);
    }

    private String generateStorageKey(String userId, String originalFilename) {
        String shortUserId = userId.replace("-", "").substring(0, 12);
        String uuid = UUID.randomUUID().toString();
        String extension = extractExtension(originalFilename);
        return shortUserId + "/" + uuid + extension;
    }

    private String extractExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex).toLowerCase();
    }
}
