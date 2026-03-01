package com.souzip.application.file.dto;

import com.souzip.domain.file.File;

public record FileResponse(
    Long id,
    String url,
    String originalName,
    Integer displayOrder
) {
    public static FileResponse of(File file, String url) {
        return new FileResponse(
            file.getId(),
            url,
            file.getOriginalName(),
            file.getDisplayOrder()
        );
    }
}
