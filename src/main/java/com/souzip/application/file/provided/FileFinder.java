package com.souzip.application.file.provided;

import com.souzip.application.file.dto.FileResponse;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.file.File;
import java.util.List;
import java.util.Map;

public interface FileFinder {

    List<File> findByEntity(EntityType entityType, Long entityId);

    File findFirst(EntityType entityType, Long entityId);

    Map<Long, File> findThumbnailsByEntityIds(EntityType entityType, List<Long> entityIds);

    List<FileResponse> findFileResponsesByEntity(EntityType entityType, Long entityId);
}
