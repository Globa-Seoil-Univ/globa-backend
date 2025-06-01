package org.y2k2.globa.application.comment.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.y2k2.globa.application.user.dto.common.UserIntroDto;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ReplyDto {
    private Long commentId;
    private String content;
    private UserIntroDto user;
    private String createdTime;
    private Boolean deleted;
}
