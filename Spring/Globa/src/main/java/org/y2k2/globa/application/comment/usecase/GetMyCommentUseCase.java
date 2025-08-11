package org.y2k2.globa.application.comment.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.comment.command.GetMyCommentCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;

@Component
@RequiredArgsConstructor
public class GetMyCommentUseCase implements UseCase<GetMyCommentCommand, CommentEntity> {
    private final CommentRepository commentRepository;

    @Override
    public CommentEntity execute(GetMyCommentCommand command) {
        CommentEntity comment = commentRepository.getComment(command.highlightId(), command.commentId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));

        boolean isNotOwner = !comment.getUser().getUserId().equals(command.userId());
        if (isNotOwner) {
            throw new CustomException(ErrorCode.MISMATCH_COMMENT_OWNER);
        }

        return comment;
    }
}
