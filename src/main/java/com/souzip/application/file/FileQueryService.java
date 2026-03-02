package com.souzip.application.file;

import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.file.provided.FileFinder;
import com.souzip.application.file.required.FileRepository;
import com.souzip.application.file.required.FileStorage;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.file.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FileQueryService implements FileFinder {

    private final FileRepository fileRepository;
    private final FileStorage fileStorage;

    @Override
    public List<File> findByEntity(EntityType entityType, Long entityId) {
        return fileRepository.findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(entityType, entityId);
    }

    @Override
    public File findFirst(EntityType entityType, Long entityId) {
        return fileRepository
                .findFirstByEntityTypeAndEntityIdOrderByDisplayOrderAsc(entityType, entityId)
                .orElseThrow(() -> new FileNotFoundException(entityType, entityId));
    }

    @Override
    public Map<Long, File> findThumbnailsByEntityIds(EntityType entityType, List<Long> entityIds) {
        if (isEmptyEntityIds(entityIds)) {
            return Map.of();
        }

        List<File> files = fileRepository.findByEntityTypeAndEntityIdInAndDisplayOrderOrderByDisplayOrder(
                entityType,
                entityIds,
                1
        );

        return files.stream().collect(
                Collectors.toMap(
                        File::getEntityId,
                        file -> file,
                        (existing, replacement) -> existing
                )
        );
    }

    @Override
    public List<FileResponse> findFileResponsesByEntity(EntityType entityType, Long entityId) {
        List<File> files = findByEntity(entityType, entityId);

        return files.stream()
                .map(this::toFileResponse)
                .toList();
    }

    private FileResponse toFileResponse(File file) {
        String url = fileStorage.generateUrl(file.getStorageKey());

        return FileResponse.of(file, url);
    }

    private boolean isEmptyEntityIds(List<Long> entityIds) {
        return entityIds == null || entityIds.isEmpty();
    }
}
