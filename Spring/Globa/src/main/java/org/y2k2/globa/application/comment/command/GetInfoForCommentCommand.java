package org.y2k2.globa.application.comment.command;

import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;

public record GetInfoForCommentCommand(
        RequestCommentWithIdsDto dto
) {
    public static GetInfoForCommentCommand of(RequestCommentWithIdsDto dto) {
        return new GetInfoForCommentCommand(dto);
    }
}
