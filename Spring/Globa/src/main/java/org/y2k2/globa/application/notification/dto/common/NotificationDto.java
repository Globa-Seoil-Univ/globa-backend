package org.y2k2.globa.application.notification.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDto {
    private Long notificationId;
    private String type;
    private Boolean isRead;
    private String createdTime;

    // 공지사항 알림
    private Notice notice;

    // 공유 초대 알림
    private User user;
    private Share share;
    private Folder folder;

    // 공유 폴더 내에 파일 추가 알림
    private Record record;

    // 공유 폴더 내에 댓글 추가 알림
    private Comment comment;

    // 문의 답변 도착
    private Inquiry inquiry;

    @Data
    public static class Notice {
        private Long noticeId;
        private String thumbnail;
        private String title;
        private String content;
    }

    @Data
    public static class User {
        private String profile;
        private String name;
    }

    @Data
    public static class Share {
        private Long shareId;
    }

    @Data
    public static class Folder {
        private Long folderId;
        private String title;
    }

    @Data
    public static class Record {
        private Long recordId;
        private String title;
    }

    @Data
    public static class Comment {
        private Long commentId;
        private String content;
    }

    @Data
    public static class Inquiry {
        private Long inquiryId;
        private String title;
    }
}
