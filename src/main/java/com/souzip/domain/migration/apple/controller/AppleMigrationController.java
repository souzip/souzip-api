package com.souzip.domain.migration.apple.controller;

import com.souzip.domain.migration.apple.dto.AppleMigrationPrepareResponse;
import com.souzip.domain.migration.apple.service.AppleMigrationPrepService;
import com.souzip.domain.migration.apple.service.AppleMigrationPrepService.MigrationResult;
import com.souzip.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/migration/apple")
@RequiredArgsConstructor
public class AppleMigrationController {

    private final AppleMigrationPrepService migrationPrepService;

    @PostMapping("/prepare")
    public SuccessResponse<AppleMigrationPrepareResponse> prepare() {
        log.info("Apple 마이그레이션 준비 API 호출");

        MigrationResult result = migrationPrepService.prepareAllAppleUsers();
        AppleMigrationPrepareResponse data = AppleMigrationPrepareResponse.from(result);

        return SuccessResponse.of(data, "마이그레이션 준비 완료");
    }
}
