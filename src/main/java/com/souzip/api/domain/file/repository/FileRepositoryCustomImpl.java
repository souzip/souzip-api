package com.souzip.api.domain.file.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.souzip.api.domain.file.entity.File;
import com.souzip.api.domain.file.entity.QFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class FileRepositoryCustomImpl implements FileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<File> findThumbnailsByEntityIds(String entityType, List<Long> entityIds) {
        QFile f = QFile.file;

        if (entityIds == null || entityIds.isEmpty()) {
            return List.of();
        }

        return queryFactory.selectFrom(f)
            .where(
                f.entityType.eq(entityType),
                f.entityId.in(entityIds),
                f.displayOrder.eq(1)
            )
            .orderBy(f.displayOrder.asc())
            .fetch();
    }
}
