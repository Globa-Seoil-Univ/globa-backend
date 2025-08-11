package org.y2k2.globa.infrastructure.persistence.comment.projection;

import java.time.LocalDateTime;

public interface CommentWithHasReplyProjection {
    Long getCommentId();
    String getContent();
    Long getUserId();
    String getProfilePath();
    String getName();
    Boolean getHasReply();
    Boolean getIsDeleted();
    LocalDateTime getCreatedTime();
}
