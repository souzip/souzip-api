package com.souzip.application.notice.assembler;

import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.file.provided.FileFinder;
import com.souzip.application.notice.dto.NoticeAuthorResponse;
import com.souzip.application.notice.dto.NoticeResponse;
import com.souzip.domain.admin.model.Admin;
import com.souzip.domain.admin.repository.AdminRepository;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.notice.Notice;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class NoticeResponseAssembler {

    private final FileFinder fileFinder;
    private final AdminRepository adminRepository;

    public NoticeResponse assemble(Notice notice) {
        Map<Long, List<FileResponse>> filesMap = fetchFilesMap(List.of(notice));
        Map<UUID, NoticeAuthorResponse> authorMap = fetchAuthorMap(List.of(notice));

        return toResponse(notice, filesMap, authorMap);
    }

    public List<NoticeResponse> assembleAll(List<Notice> notices) {
        if (notices.isEmpty()) {
            return List.of();
        }

        Map<Long, List<FileResponse>> filesMap = fetchFilesMap(notices);
        Map<UUID, NoticeAuthorResponse> authorMap = fetchAuthorMap(notices);

        return notices.stream()
                .map(n -> toResponse(n, filesMap, authorMap))
                .toList();
    }

    private NoticeResponse toResponse(
            Notice notice,
            Map<Long, List<FileResponse>> filesMap,
            Map<UUID, NoticeAuthorResponse> authorMap
    ) {
        List<FileResponse> files = filesMap.getOrDefault(notice.getId(), List.of());

        NoticeAuthorResponse author = authorMap.get(notice.getAuthorId());

        return NoticeResponse.from(notice, author, files);
    }

    private Map<Long, List<FileResponse>> fetchFilesMap(List<Notice> notices) {
        List<Long> ids = notices.stream().map(Notice::getId).toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return fileFinder.findFilesByEntityIds(EntityType.NOTICE, ids);
    }

    private Map<UUID, NoticeAuthorResponse> fetchAuthorMap(List<Notice> notices) {
        List<UUID> authorIds = notices.stream()
                .map(Notice::getAuthorId)
                .distinct()
                .toList();

        if (authorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return adminRepository.findAllByIds(authorIds).stream()
                .map(this::toAuthorResponse)
                .collect(Collectors.toMap(NoticeAuthorResponse::authorId, Function.identity()));
    }

    private NoticeAuthorResponse toAuthorResponse(Admin admin) {
        return NoticeAuthorResponse.of(
                admin.getId(),
                admin.getUsername().value()
        );
    }
}
