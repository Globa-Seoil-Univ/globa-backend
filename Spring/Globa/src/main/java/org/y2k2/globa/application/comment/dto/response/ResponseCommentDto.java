package org.y2k2.globa.application.comment.dto.response;

import org.y2k2.globa.application.comment.dto.common.CommentDto;

import java.util.List;

public record ResponseCommentDto(
    List<CommentDto> comments,
    Long total
) {
}