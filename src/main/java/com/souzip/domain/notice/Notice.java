package com.souzip.domain.notice;

import com.souzip.domain.shared.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

    private String title;

    private String content;

    private Long authorId;

    private NoticeStatus status;

    public static Notice register(NoticeRegisterRequest request) {
        Notice notice = new Notice();

        notice.title = requireNonNull(request.title(), "제목은 필수입니다.");
        notice.content = requireNonNull(request.content(), "내용은 필수입니다.");
        notice.authorId = requireNonNull(request.authorId(), "작성자는 필수입니다.");
        notice.status = requireNonNull(request.status(), "상태는 필수입니다.");

        return notice;
    }

    public void update(NoticeUpdateRequest request) {
        this.title = requireNonNull(request.title(), "제목은 필수입니다.");
        this.content = requireNonNull(request.content(), "내용은 필수입니다.");
        this.status = requireNonNull(request.status(), "상태는 필수입니다.");
    }

    public void activate() {
        this.status = NoticeStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = NoticeStatus.INACTIVE;
    }

    public boolean isActive() {
        return this.status == NoticeStatus.ACTIVE;
    }
}
