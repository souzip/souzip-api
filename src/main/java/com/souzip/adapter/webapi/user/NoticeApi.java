package com.souzip.adapter.webapi.user;

import com.souzip.application.notice.dto.NoticeResponse;
import com.souzip.application.notice.provided.NoticeFinder;
import com.souzip.shared.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/notices")
@RequiredArgsConstructor
@RestController
public class NoticeApi {

    private final NoticeFinder noticeFinder;

    @GetMapping
    public SuccessResponse<List<NoticeResponse>> getAllActive() {
        List<NoticeResponse> response = noticeFinder.findAllActiveWithFiles();

        return SuccessResponse.of(response);
    }

    @GetMapping("/{noticeId}")
    public SuccessResponse<NoticeResponse> getById(@PathVariable Long noticeId) {
        NoticeResponse response = noticeFinder.findActiveByIdWithFiles(noticeId);

        return SuccessResponse.of(response);
    }
}
