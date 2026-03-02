package com.souzip.application.file.required;

import com.souzip.domain.file.EntityType;
import com.souzip.domain.file.File;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface FileRepository extends Repository<File, Long> {

    File save(File file);

    Optional<File> findById(Long id);

    List<File> findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(EntityType entityType, Long entityId);

    Optional<File> findFirstByEntityTypeAndEntityIdOrderByDisplayOrderAsc(EntityType entityType, Long entityId);

    void delete(File file);

    List<File> findByEntityTypeAndEntityIdInAndDisplayOrderOrderByDisplayOrder(
            EntityType entityType,
            List<Long> entityIds,
            Integer displayOrder
    );
}
