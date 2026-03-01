package com.souzip.application.file.required;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 스토리지에 파일을 업로드/삭제/URL 생성한다
 */
public interface FileStorage {

    String upload(String userId, MultipartFile file);

    void delete(String storageKey);

    String generateUrl(String storageKey);
}
