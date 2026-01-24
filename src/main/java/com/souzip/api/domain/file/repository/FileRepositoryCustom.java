package com.souzip.api.domain.file.repository;

import com.souzip.api.domain.file.entity.File;

import java.util.List;

public interface FileRepositoryCustom {
    List<File> findThumbnailsByEntityIds(String entityType, List<Long> entityIds);
}
