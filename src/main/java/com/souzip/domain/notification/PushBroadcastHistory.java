package com.souzip.domain.notification;

import com.souzip.shared.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushBroadcastHistory extends BaseEntity {

    private UUID adminId;

    private String title;

    private String body;

    private int totalTargets;

    private int successCount;

    private int failCount;

    private boolean firebaseConfigured;

    public static PushBroadcastHistory record(
            UUID adminId,
            String title,
            String body,
            int totalTargets,
            int successCount,
            int failCount,
            boolean firebaseConfigured
    ) {
        PushBroadcastHistory row = new PushBroadcastHistory();
        row.adminId = requireNonNull(adminId, "관리자 ID는 필수입니다.");
        row.title = requireNonNull(title, "제목은 필수입니다.");
        row.body = requireNonNull(body, "본문은 필수입니다.");
        row.totalTargets = totalTargets;
        row.successCount = successCount;
        row.failCount = failCount;
        row.firebaseConfigured = firebaseConfigured;
        return row;
    }
}
