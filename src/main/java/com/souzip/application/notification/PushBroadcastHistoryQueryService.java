package com.souzip.application.notification;

import com.souzip.application.notification.dto.PushBroadcastHistoryResponse;
import com.souzip.application.notification.required.PushBroadcastHistoryRepository;
import com.souzip.domain.notification.PushBroadcastHistory;
import com.souzip.global.common.dto.pagination.PaginationRequest;
import com.souzip.global.common.dto.pagination.PaginationResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushBroadcastHistoryQueryService {

    private final PushBroadcastHistoryRepository pushBroadcastHistoryRepository;

    // 푸시 브로드캐스트 이력을 최신순 페이지로 조회합니다.
    public PaginationResponse<PushBroadcastHistoryResponse> findPage(PaginationRequest paginationRequest) {
        Page<PushBroadcastHistory> page =
                pushBroadcastHistoryRepository.findAllByOrderByCreatedAtDesc(paginationRequest.toPageable());
        List<PushBroadcastHistoryResponse> content = page.getContent().stream()
                .map(PushBroadcastHistoryResponse::from)
                .toList();
        return PaginationResponse.of(page, content);
    }
}
