package com.souzip.adapter.webapi.admin;

import com.souzip.adapter.webapi.admin.dto.NoticeRequest;
import com.souzip.application.notice.dto.NoticeResponse;
import com.souzip.application.notice.provided.NoticeFinder;
import com.souzip.application.notice.provided.NoticeRegister;
import com.souzip.domain.admin.infrastructure.security.annotation.AdminAccess;
import com.souzip.domain.admin.infrastructure.security.annotation.CurrentAdminId;
import com.souzip.domain.admin.infrastructure.security.annotation.ViewerAccess;
import com.souzip.domain.notice.Notice;
import com.souzip.global.common.dto.SuccessResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
@RestController
public class AdminNoticeApi {

    private final NoticeRegister noticeRegister;
    private final NoticeFinder noticeFinder;

    @AdminAccess
    @PostMapping
    public SuccessResponse<NoticeResponse> register(
            @CurrentAdminId UUID adminId,
            @RequestPart("notice") NoticeRequest request,
            @RequestPart(value = "files", required = false) Optional<List<MultipartFile>> files
    ) {
        Notice notice = noticeRegister.register(
                request.toDomain(adminId),
                files.orElse(List.of())
        );

        NoticeResponse response = noticeFinder.findByIdWithFiles(notice.getId());

        return SuccessResponse.of(response, "공지사항이 등록되었습니다.");
    }

    @AdminAccess
    @PutMapping("/{noticeId}")
    public SuccessResponse<NoticeResponse> update(
            @PathVariable Long noticeId,
            @RequestPart("notice") NoticeRequest request,
            @RequestPart(value = "deleteFileIds", required = false) Optional<List<Long>> deleteFileIds,
            @RequestPart(value = "newFiles", required = false) Optional<List<MultipartFile>> newFiles
    ) {
        Notice notice = noticeRegister.update(
                noticeId,
                request.toDomain(),
                deleteFileIds.orElse(List.of()),
                newFiles.orElse(List.of())
        );

        NoticeResponse response = noticeFinder.findByIdWithFiles(notice.getId());

        return SuccessResponse.of(response, "공지사항이 수정되었습니다.");
    }

    @AdminAccess
    @PatchMapping("/{noticeId}/activate")
    public SuccessResponse<Void> activate(@PathVariable Long noticeId) {
        noticeRegister.activate(noticeId);

        return SuccessResponse.of("공지사항이 활성화되었습니다.");
    }

    @AdminAccess
    @PatchMapping("/{noticeId}/deactivate")
    public SuccessResponse<Void> deactivate(@PathVariable Long noticeId) {
        noticeRegister.deactivate(noticeId);

        return SuccessResponse.of("공지사항이 비활성화되었습니다.");
    }

    @AdminAccess
    @DeleteMapping("/{noticeId}")
    public SuccessResponse<Void> delete(@PathVariable Long noticeId) {
        noticeRegister.delete(noticeId);

        return SuccessResponse.of("공지사항이 삭제되었습니다.");
    }

    @ViewerAccess
    @GetMapping
    public SuccessResponse<List<NoticeResponse>> getAll() {
        List<NoticeResponse> response = noticeFinder.findAllWithFiles();

        return SuccessResponse.of(response);
    }

    @ViewerAccess
    @GetMapping("/{noticeId}")
    public SuccessResponse<NoticeResponse> getById(@PathVariable Long noticeId) {
        NoticeResponse response = noticeFinder.findByIdWithFiles(noticeId);

        return SuccessResponse.of(response);
    }
}
