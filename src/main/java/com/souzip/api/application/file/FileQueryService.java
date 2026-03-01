package com.souzip.api.application.file;

import com.souzip.api.application.file.provided.FileFinder;
import com.souzip.api.application.file.required.FileRepository;
import com.souzip.api.domain.file.File;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FileQueryService implements FileFinder {

    private final FileRepository fileRepository;

    @Override
    public List<File> findByEntity(String entityType, Long entityId) {
        return fileRepository
                .findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(entityType, entityId);
    }

    @Override
    public File findFirst(String entityType, Long entityId) {
        return fileRepository
                .findFirstByEntityTypeAndEntityIdOrderByDisplayOrderAsc(entityType, entityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
    }

    @Override
    public Map<Long, File> findThumbnailsByEntityIds(String entityType, List<Long> entityIds) {
        if (entityIds == null || entityIds.isEmpty()) {
            return Map.of();
        }

        List<File> files = fileRepository
                .findByEntityTypeAndEntityIdInAndDisplayOrderOrderByDisplayOrder(
                        entityType,
                        entityIds,
                        1
                );

        return files.stream()
                .collect(Collectors.toMap(
                        File::getEntityId,
                        file -> file,
                        (existing, replacement) -> existing
                ));
    }
}
