package org.y2k2.globa.application.comment.command;

public record GetMyCommentCommand(
        Long userId,
        Long highlightId,
        Long commentId
) {
    public static GetMyCommentCommand of(Long userId, Long highlightId, Long commentId) {
        return new GetMyCommentCommand(userId, highlightId, commentId);
    }
}
