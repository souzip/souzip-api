package com.souzip.application.notice;

public class NoticeNotFoundException extends RuntimeException {

    public NoticeNotFoundException(Long noticeId) {
        super("공지사항을 찾을 수 없습니다. id: " + noticeId);
    }
}
