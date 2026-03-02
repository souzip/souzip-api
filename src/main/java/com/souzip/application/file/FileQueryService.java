package com.souzip.application.file;

import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.file.provided.FileFinder;
import com.souzip.application.file.required.FileRepository;
import com.souzip.application.file.required.FileStorage;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.file.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        return mapThumbnailsByEntityId(files);
    }

    @Override
    public List<FileResponse> findFileResponsesByEntity(EntityType entityType, Long entityId) {
        List<File> files = findByEntity(entityType, entityId);
        return convertToFileResponses(files);
    }

    @Override
    public Map<Long, List<FileResponse>> findFilesByEntityIds(EntityType entityType, List<Long> entityIds) {
        if (isEmptyEntityIds(entityIds)) {
            return Map.of();
        }

        List<File> files = fileRepository.findByEntityTypeAndEntityIdIn(entityType, entityIds);
        return groupFilesByEntityId(files);
    }

    private Map<Long, File> mapThumbnailsByEntityId(List<File> files) {
        Map<Long, File> thumbnails = new HashMap<>();

        for (File file : files) {
            thumbnails.putIfAbsent(file.getEntityId(), file);
        }

        return thumbnails;
    }

    private List<FileResponse> convertToFileResponses(List<File> files) {
        List<FileResponse> responses = new ArrayList<>();

        for (File file : files) {
            responses.add(toFileResponse(file));
        }

        return responses;
    }

    private Map<Long, List<FileResponse>> groupFilesByEntityId(List<File> files) {
        Map<Long, List<FileResponse>> result = new HashMap<>();

        for (File file : files) {
            FileResponse response = toFileResponse(file);
            result.computeIfAbsent(file.getEntityId(), k -> new ArrayList<>())
                    .add(response);
        }

        return result;
    }

    private FileResponse toFileResponse(File file) {
        String url = fileStorage.generateUrl(file.getStorageKey());
        return FileResponse.of(file, url);
    }

    private boolean isEmptyEntityIds(List<Long> entityIds) {
        return entityIds == null || entityIds.isEmpty();
    }
}
