package org.y2k2.globa.application.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.comment.dto.common.ReplyDto;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.dto.response.ResponseReplyDto;
import org.y2k2.globa.application.comment.mapper.CommentMapper;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetRepliesService {
    private final VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;

    private final HighlightRepository highlightRepository;
    private final CommentRepository commentRepository;

    public ResponseReplyDto get(RequestCommentWithIdsDto dto, int page, int count) {
        verifyFolderAccessibleUseCase.execute(
                VerifyFolderCommand.of(dto.userId(), dto.folderId())
        );

        Boolean isHighlightInSection = highlightRepository.isHighlightInSection(dto.sectionId(), dto.highlightId());
        if (!isHighlightInSection) {
            throw new CustomException(ErrorCode.NOT_FOUND_HIGHLIGHT);
        }

        Boolean isExistParentComment = commentRepository.isExistParentComment(dto.highlightId(), dto.parentId());
        if (!isExistParentComment) {
            throw new CustomException(ErrorCode.NOT_FOUND_PARENT_COMMENT);
        }

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<CommentEntity> comments = commentRepository.getChildComments(dto.parentId(), pageable);

        List<ReplyDto> response = comments.getContent().stream()
                .map(CommentMapper.INSTANCE::toResponseReplyDto)
                .toList();

        return new ResponseReplyDto(
                response,
                comments.getTotalElements()
        );
    }
}
