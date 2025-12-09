package com.souzip.api.domain.file.service;

import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.file.entity.File;
import com.souzip.api.domain.file.repository.FileRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class FileService {

    private final FileStorageService fileStorageService;
    private final FileRepository fileRepository;

    @Transactional
    public FileResponse uploadFile(
        String userId,
        String entityType,
        Long entityId,
        MultipartFile file,
        Integer displayOrder
    ) {

        String storageKey = fileStorageService.uploadFile(userId, file);
        Integer order = getDisplayOrder(entityType, entityId, displayOrder);

        File savedFile = fileRepository.save(
            File.of(entityType, entityId, storageKey,
                file.getOriginalFilename(), file.getSize(),
                file.getContentType(), order)
        );

        String url = fileStorageService.generatePresignedUrl(storageKey);
        return FileResponse.of(savedFile, url);
    }

    public List<FileResponse> getFilesByEntity(String entityType, Long entityId) {
        List<File> files = fileRepository
            .findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(entityType, entityId);

        return files.stream()
            .map(this::toFileResponse)
            .toList();
    }

    public FileResponse getFirstFile(String entityType, Long entityId) {
        File file = fileRepository
            .findFirstByEntityTypeAndEntityIdOrderByDisplayOrderAsc(entityType, entityId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        return toFileResponse(file);
    }

    @Transactional
    public void deleteFile(Long fileId) {
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        fileStorageService.deleteFile(file.getStorageKey());
        fileRepository.delete(file);
    }

    @Transactional
    public void deleteFilesByEntity(String entityType, Long entityId) {
        List<File> files = fileRepository
            .findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(entityType, entityId);

        files.forEach(file -> fileStorageService.deleteFile(file.getStorageKey()));
        fileRepository.deleteByEntityTypeAndEntityId(entityType, entityId);
    }

    @Transactional
    public void updateDisplayOrder(Long fileId, Integer newDisplayOrder) {
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        file.updateDisplayOrder(newDisplayOrder);
    }

    private Integer getDisplayOrder(String entityType, Long entityId, Integer displayOrder) {
        if (displayOrder != null) {
            return displayOrder;
        }
        return calculateNextDisplayOrder(entityType, entityId);
    }

    private Integer calculateNextDisplayOrder(String entityType, Long entityId) {
        List<File> existingFiles = fileRepository
            .findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(entityType, entityId);

        if (existingFiles.isEmpty()) {
            return 1;
        }

        File lastFile = existingFiles.getLast();
        return lastFile.getDisplayOrder() + 1;
    }

    private FileResponse toFileResponse(File file) {
        String url = fileStorageService.generatePresignedUrl(file.getStorageKey());
        return FileResponse.of(file, url);
    }
}
