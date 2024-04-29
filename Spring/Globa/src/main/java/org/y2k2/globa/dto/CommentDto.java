package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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