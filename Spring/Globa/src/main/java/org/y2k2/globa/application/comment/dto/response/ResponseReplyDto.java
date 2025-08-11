package org.y2k2.globa.application.comment.dto.response;

import org.y2k2.globa.application.comment.dto.common.ReplyDto;

import java.util.List;

public record ResponseReplyDto(List<ReplyDto> comments, Long total) {
}
