package com.souzip.application.notice.provided;

import com.souzip.domain.notice.Notice;
import com.souzip.domain.notice.NoticeRegisterRequest;
import com.souzip.domain.notice.NoticeUpdateRequest;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface NoticeRegister {

    Notice register(NoticeRegisterRequest noticeRegisterRequest, List<MultipartFile> files);

    Notice update(
            Long noticeId,
            NoticeUpdateRequest noticeUpdateRequest,
            List<Long> deleteFileIds,
            List<MultipartFile> newFiles
    );

    void activate(Long noticeId);

    void deactivate(Long noticeId);

    void delete(Long noticeId);
}
