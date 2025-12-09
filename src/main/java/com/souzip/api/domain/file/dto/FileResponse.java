package com.souzip.api.domain.file.dto;

import com.souzip.api.domain.file.entity.File;

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
