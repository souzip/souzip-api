package com.souzip.api.domain.file.repository;

import com.souzip.api.domain.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(String entityType, Long entityId);

    Optional<File> findFirstByEntityTypeAndEntityIdOrderByDisplayOrderAsc(String entityType, Long entityId);

    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);
}
