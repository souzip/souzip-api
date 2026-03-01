package com.souzip.api.application.file.required;

import com.souzip.api.domain.file.File;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * 파일 정보를 저장하거나 조회한다
 */
public interface FileRepository extends Repository<File, Long> {

    File save(File file);

    Optional<File> findById(Long id);

    List<File> findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(String entityType, Long entityId);

    Optional<File> findFirstByEntityTypeAndEntityIdOrderByDisplayOrderAsc(String entityType, Long entityId);

    void delete(File file);

    List<File> findByEntityTypeAndEntityIdInAndDisplayOrderOrderByDisplayOrder(
            String entityType,
            List<Long> entityIds,
            Integer displayOrder
    );
}
