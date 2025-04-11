package org.y2k2.globa.application.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.comment.command.GetMyCommentCommand;
import org.y2k2.globa.application.comment.dto.request.RequestCommentDto;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.usecase.GetMyCommentUseCase;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyWritableUseCase;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;

@Service
@RequiredArgsConstructor
public class UpdateCommentService {
    private final VerifyWritableUseCase verifyWritableUseCase;
    private final GetMyCommentUseCase getMyCommentUseCase;

    private final CommentRepository commentRepository;

    public void update(RequestCommentWithIdsDto idsDto, Long commentId, RequestCommentDto request) {
        verifyWritableUseCase.execute(VerifyFolderCommand.of(idsDto.userId(), idsDto.folderId()));

        CommentEntity comment = getMyCommentUseCase.execute(
                GetMyCommentCommand.of(idsDto.userId(), idsDto.highlightId(), commentId)
        );

        comment.updateComment(request.content());
        commentRepository.save(comment);
    }
}
