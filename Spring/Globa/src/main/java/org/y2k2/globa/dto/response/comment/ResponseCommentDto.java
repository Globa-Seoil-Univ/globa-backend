package org.y2k2.globa.dto.response.comment;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.dto.common.comment.CommentDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResponseCommentDto {
    List<CommentDto> comments;
    long total = 0;
}