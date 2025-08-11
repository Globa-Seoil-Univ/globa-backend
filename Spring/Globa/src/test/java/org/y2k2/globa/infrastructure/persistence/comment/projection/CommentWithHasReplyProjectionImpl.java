package org.y2k2.globa.infrastructure.persistence.comment.projection;

import java.time.LocalDateTime;

public class CommentWithHasReplyProjectionImpl implements CommentWithHasReplyProjection {
    private Long commentId;
    private String content;
    private Long userId;
    private String profilePath;
    private String name;
    private Boolean hasReply;
    private Boolean isDeleted;
    private LocalDateTime createdTime;

    public CommentWithHasReplyProjectionImpl(Long commentId, String content, Long userId, String profilePath, String name, Boolean hasReply, Boolean isDeleted, LocalDateTime createdTime) {
        this.commentId = commentId;
        this.content = content;
        this.userId = userId;
        this.profilePath = profilePath;
        this.name = name;
        this.hasReply = hasReply;
        this.isDeleted = isDeleted;
        this.createdTime = createdTime;
    }

    @Override
    public Long getCommentId() {
        return commentId;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public String getProfilePath() {
        return profilePath;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Boolean getHasReply() {
        return hasReply;
    }

    @Override
    public Boolean getIsDeleted() {
        return isDeleted;
    }

    @Override
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
}
