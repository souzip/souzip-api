package com.souzip.application.file.provided;

import com.souzip.domain.file.File;
import java.util.List;
import java.util.Map;

public interface FileFinder {

    List<File> findByEntity(String entityType, Long entityId);

    File findFirst(String entityType, Long entityId);

    Map<Long, File> findThumbnailsByEntityIds(String entityType, List<Long> entityIds);
}
