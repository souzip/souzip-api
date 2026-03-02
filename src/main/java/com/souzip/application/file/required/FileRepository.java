package com.souzip.application.file.required;

import com.souzip.domain.file.EntityType;
import com.souzip.domain.file.File;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface FileRepository extends Repository<File, Long> {

    File save(File file);

    Optional<File> findById(Long id);

    List<File> findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(EntityType entityType, Long entityId);

    Optional<File> findFirstByEntityTypeAndEntityIdOrderByDisplayOrderAsc(EntityType entityType, Long entityId);

    void delete(File file);

    @Query("SELECT f FROM File f WHERE f.entityType = :entityType AND f.entityId IN :entityIds AND f.displayOrder = :displayOrder ORDER BY f.displayOrder")
    List<File> findByEntityTypeAndEntityIdInAndDisplayOrderOrderByDisplayOrder(
            @Param("entityType") EntityType entityType,
            @Param("entityIds") List<Long> entityIds,
            @Param("displayOrder") Integer displayOrder
    );

    @Query("SELECT f FROM File f WHERE f.entityType = :entityType AND f.entityId IN :entityIds ORDER BY f.entityId, f.displayOrder")
    List<File> findByEntityTypeAndEntityIdIn(
            @Param("entityType") EntityType entityType,
            @Param("entityIds") List<Long> entityIds
    );
}
