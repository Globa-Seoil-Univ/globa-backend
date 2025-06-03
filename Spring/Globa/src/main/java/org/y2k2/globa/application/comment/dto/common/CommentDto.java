package org.y2k2.globa.application.comment.dto.common;

import lombok.*;
import org.y2k2.globa.application.user.dto.common.UserIntroDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentDto {
    private Long commentId;
    private String content;
    private UserIntroDto user;
    private String createdTime;
    private Boolean hasReply;
    private Boolean deleted;
}