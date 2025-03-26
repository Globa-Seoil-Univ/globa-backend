package org.y2k2.globa.application.comment.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.application.comment.dto.common.CommentDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResponseCommentDto {
    List<CommentDto> comments;
    long total = 0;
}