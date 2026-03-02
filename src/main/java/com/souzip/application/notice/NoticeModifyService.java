package com.souzip.application.notice;

import com.souzip.application.file.provided.FileModifier;
import com.souzip.application.notice.provided.NoticeFinder;
import com.souzip.application.notice.provided.NoticeRegister;
import com.souzip.application.notice.required.NoticeRepository;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.notice.Notice;
import com.souzip.domain.notice.NoticeRegisterRequest;
import com.souzip.domain.notice.NoticeUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class NoticeModifyService implements NoticeRegister {

    private final NoticeRepository noticeRepository;
    private final NoticeFinder noticeFinder;
    private final FileModifier fileModifier;

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

        deleteFiles(deleteFileIds);

        uploadFiles(noticeId, notice.getAuthorId().toString(), newFiles);

        return noticeRepository.save(notice);
    }

    @Override
    public void activate(Long noticeId) {
        Notice notice = findNoticeById(noticeId);

        notice.activate();

        noticeRepository.save(notice);
    }

    @Override
    public void deactivate(Long noticeId) {
        Notice notice = findNoticeById(noticeId);

        notice.deactivate();

        noticeRepository.save(notice);
    }

    @Override
    public void delete(Long noticeId) {
        Notice notice = findNoticeById(noticeId);

        fileModifier.deleteByEntity(EntityType.NOTICE, noticeId);

        noticeRepository.delete(notice);
    }

    private Notice findNoticeById(Long noticeId) {
        return noticeFinder.findById(noticeId);
    }

    private void deleteFiles(List<Long> deleteFileIds) {
        if (deleteFileIds == null || deleteFileIds.isEmpty()) {
            return;
        }

        deleteFileIds.forEach(fileModifier::delete);
    }

    private void uploadFiles(Long noticeId, String userId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        files.forEach(file ->
                fileModifier.register(userId, EntityType.NOTICE, noticeId, file, null)
        );
    }
}
