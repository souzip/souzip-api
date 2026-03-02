package com.souzip.application.notice;

import com.souzip.application.file.provided.FileFinder;
import com.souzip.application.file.provided.FileModifier;
import com.souzip.application.notice.provided.NoticeFinder;
import com.souzip.application.notice.provided.NoticeRegister;
import com.souzip.application.notice.required.NoticeRepository;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.file.File;
import com.souzip.domain.notice.Notice;
import com.souzip.domain.notice.NoticeRegisterRequest;
import com.souzip.domain.notice.NoticeUpdateRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@RequiredArgsConstructor
@Service
public class NoticeModifyService implements NoticeRegister {

    private final NoticeRepository noticeRepository;
    private final NoticeFinder noticeFinder;
    private final FileModifier fileModifier;
    private final FileFinder fileFinder;

    @Override
    public Notice register(NoticeRegisterRequest registerRequest, List<MultipartFile> files) {
        Notice notice = Notice.register(registerRequest);
        Notice savedNotice = noticeRepository.save(notice);

        uploadFiles(savedNotice.getId(), registerRequest.authorId().toString(), files);

        return savedNotice;
    }

    @Override
    public Notice update(
            Long noticeId,
            NoticeUpdateRequest updateRequest,
            List<Long> deleteFileIds,
            List<MultipartFile> newFiles
    ) {
        Notice notice = noticeFinder.findById(noticeId);
        notice.update(updateRequest);

        deleteFiles(noticeId, deleteFileIds);
        uploadFiles(noticeId, notice.getAuthorId().toString(), newFiles);

        return notice;
    }

    @Override
    public void activate(Long noticeId) {
        Notice notice = noticeFinder.findById(noticeId);
        notice.activate();
    }

    @Override
    public void deactivate(Long noticeId) {
        Notice notice = noticeFinder.findById(noticeId);
        notice.deactivate();
    }

    @Override
    public void delete(Long noticeId) {
        Notice notice = noticeFinder.findById(noticeId);

        fileModifier.deleteByEntity(EntityType.NOTICE, noticeId);
        noticeRepository.delete(notice);
    }

    private void deleteFiles(Long noticeId, List<Long> deleteFileIds) {
        if (isEmpty(deleteFileIds)) {
            return;
        }

        List<File> noticeFiles = fileFinder.findByEntity(EntityType.NOTICE, noticeId);
        Set<Long> validFileIds = noticeFiles.stream()
                .map(File::getId)
                .collect(Collectors.toSet());

        deleteFileIds.stream()
                .filter(validFileIds::contains)
                .forEach(fileModifier::delete);
    }

    private void uploadFiles(Long noticeId, String userId, List<MultipartFile> files) {
        if (isEmpty(files)) {
            return;
        }

        files.forEach(file ->
                fileModifier.register(userId, EntityType.NOTICE, noticeId, file, null)
        );
    }

    private boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
}
