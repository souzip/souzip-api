package com.souzip.application.file.required;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    String upload(String userId, MultipartFile file);

    void delete(String storageKey);

    String generateUrl(String storageKey);
}
