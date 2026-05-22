package com.souzip.domain.migration.apple.dto;

import com.souzip.domain.migration.apple.service.AppleMigrationPrepService.MigrationResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppleMigrationPrepareResponse {
    private final int total;
    private final int successCount;
    private final int failCount;
    private final int skipCount;

    public static AppleMigrationPrepareResponse from(MigrationResult result) {
        return AppleMigrationPrepareResponse.builder()
            .total(result.total())
            .successCount(result.success())
            .failCount(result.fail())
            .skipCount(result.skip())
            .build();
    }
}
