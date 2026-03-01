package com.souzip.application.file;

import com.souzip.application.file.provided.FileModifier;
import com.souzip.application.file.required.FileRepository;
import com.souzip.application.file.required.FileStorage;
import com.souzip.domain.file.File;
import com.souzip.domain.file.FileRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class FileModifyService implements FileModifier {

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
        Integer order = resolveDisplayOrder(entityType, entityId, displayOrder);

        FileRegisterRequest request = FileRegisterRequest.of(
                entityType,
                entityId,
                storageKey,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType(),
                order
        );

        File fileEntity = File.register(request);

        return fileRepository.save(fileEntity);
    }

    @Override
    public void delete(Long fileId) {
        File file = findFileById(fileId);
        deleteFileWithStorage(file);
    }

    @Override
    public void deleteByEntity(String entityType, Long entityId) {
        List<File> files = fileRepository
                .findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(entityType, entityId);

        files.forEach(this::deleteFileWithStorage);
    }

    private File findFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
    }

    private void deleteFileWithStorage(File file) {
        fileStorage.delete(file.getStorageKey());
        fileRepository.delete(file);
    }

    private Integer resolveDisplayOrder(String entityType, Long entityId, Integer displayOrder) {
        if (displayOrder != null) {
            return displayOrder;
        }

        List<File> existingFiles = fileRepository
                .findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(entityType, entityId);

        if (existingFiles.isEmpty()) {
            return 1;
        }

        return existingFiles.getLast().getDisplayOrder() + 1;
    }
}
