package org.y2k2.globa.application.comment.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CleanupCommentUseCase implements VoidUseCase<Void> {
    private final CommentRepository commentRepository;

    @Override
    public void execute(Void command) {
        List<CommentEntity> cleanupComments = commentRepository.getAllCleanupComment();

        if (cleanupComments.isEmpty()) {
            return;
        }

        LocalDateTime currentTimestamp = new CustomTimestamp().getTimestamp();

        for (CommentEntity comment : cleanupComments) {
            comment.setIsDeleted(true);
            comment.setDeletedTime(currentTimestamp);
            comment.setContent("삭제된 댓글입니다.");
        }

        commentRepository.saveAll(cleanupComments);
    }
}
