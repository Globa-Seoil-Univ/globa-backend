package org.y2k2.globa.dto.common.notification.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.dto.common.user.UserIntroDto;

@Getter
@Setter
@AllArgsConstructor
public class CommentDto {
    private long commentId;
    private String content;
    private UserIntroDto user;
    private String createdTime;
    private boolean hasReply;
    private boolean deleted;
}