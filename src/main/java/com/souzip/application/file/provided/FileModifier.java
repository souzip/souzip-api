package com.souzip.application.file.provided;

import com.souzip.domain.file.File;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 등록 및 삭제 기능을 제공한다
 */
public interface FileModifier {

    File register(String userId, String entityType, Long entityId,
                  MultipartFile file, Integer displayOrder);

    void delete(Long fileId);

    void deleteByEntity(String entityType, Long entityId);
}
