package com.souzip.application.file.provided;

import com.souzip.domain.file.EntityType;
import com.souzip.domain.file.File;
import org.springframework.web.multipart.MultipartFile;

public interface FileModifier {

    File register(String userId, EntityType entityType, Long entityId,
                  MultipartFile file, Integer displayOrder);

    void delete(Long fileId);

    void deleteByEntity(EntityType entityType, Long entityId);
}
