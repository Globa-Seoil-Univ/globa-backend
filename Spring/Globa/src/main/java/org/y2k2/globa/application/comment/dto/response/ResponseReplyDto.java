package org.y2k2.globa.application.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.application.comment.dto.common.ReplyDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResponseReplyDto {
    List<ReplyDto> comments;
    long total = 0;
}
