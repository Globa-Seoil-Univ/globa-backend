package org.y2k2.globa.application.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.comment.command.GetMyCommentCommand;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.usecase.GetMyCommentUseCase;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderWritableUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteCommentService {
    private final VerifyFolderWritableUseCase verifyFolderWritableUseCase;
    private final GetMyCommentUseCase getMyCommentUseCase;

    private final CommentRepository commentRepository;
    private final HighlightRepository highlightRepository;

    public void delete(RequestCommentWithIdsDto idsDto, Long commentId) {
        verifyFolderWritableUseCase.execute(VerifyFolderCommand.of(idsDto.userId(), idsDto.folderId()));

        CommentEntity comment = getMyCommentUseCase.execute(
                GetMyCommentCommand.of(idsDto.userId(), idsDto.highlightId(), commentId)
        );

        Boolean isLastComment = commentRepository.hasDeletedCommentInHighlight(commentId);

        if (isLastComment) {
            List<CommentEntity> deletedComments = commentRepository.getAllDeletedComment(commentId);
            if (deletedComments.isEmpty()) {
                throw new CustomException(ErrorCode.NOT_FOUND_COMMENT);
            }

            Long highlightId = deletedComments.get(0).getHighlight().getHighlightId();
            HighlightEntity highlight = highlightRepository.getHighlight(idsDto.sectionId(), highlightId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_HIGHLIGHT));

            commentRepository.deleteAll(deletedComments);
            highlightRepository.delete(highlight);
        } else {
            comment.setIsDeleted(true);
            comment.setDeletedTime(new CustomTimestamp().getTimestamp());
            commentRepository.save(comment);
        }
    }
}
