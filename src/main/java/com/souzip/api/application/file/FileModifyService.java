package com.souzip.api.application.file;

import com.souzip.api.application.file.provided.FileRegister;
import com.souzip.api.application.file.required.FileRepository;
import com.souzip.api.application.file.required.FileStorage;
import com.souzip.api.domain.file.File;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class FileModifyService implements FileRegister {

    private final FileRepository fileRepository;
    private final FileStorage fileStorage;

    @Override
    public File register(
            String userId,
            String entityType,
            Long entityId,
            MultipartFile file,
            Integer displayOrder
    ) {
        String storageKey = fileStorage.upload(userId, file);
        Integer order = getDisplayOrder(entityType, entityId, displayOrder);

        File fileEntity = File.create(
                entityType,
                entityId,
                storageKey,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType(),
                order
        );

        return fileRepository.save(fileEntity);
    }

    @Override
    public void delete(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        fileStorage.delete(file.getStorageKey());
        fileRepository.delete(file);
    }

    @Override
    public void deleteByEntity(String entityType, Long entityId) {
        List<File> files = fileRepository
                .findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(entityType, entityId);

        files.forEach(file -> fileStorage.delete(file.getStorageKey()));
        fileRepository.deleteByEntityTypeAndEntityId(entityType, entityId);
    }

    @Override
    public void updateDisplayOrder(Long fileId, Integer newDisplayOrder) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        file.updateDisplayOrder(newDisplayOrder);
        fileRepository.save(file);
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
}
