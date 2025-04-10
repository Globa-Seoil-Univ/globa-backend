package org.y2k2.globa.application.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.comment.dto.common.CommentDto;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.dto.response.ResponseCommentDto;
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
public class GetCommentsService {
    private final VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;

    private final HighlightRepository highlightRepository;
    private final CommentRepository commentRepository;

    public ResponseCommentDto get(RequestCommentWithIdsDto dto, int page, int count) {
        verifyFolderAccessibleUseCase.execute(
                VerifyFolderCommand.of(dto.userId(), dto.folderId())
        );

        Boolean isHighlightInSection = highlightRepository.isHighlightInSection(dto.sectionId(), dto.highlightId());
        if (!isHighlightInSection) {
            throw new CustomException(ErrorCode.NOT_FOUND_HIGHLIGHT);
        }

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<CommentEntity> comments = commentRepository.getParentComments(dto.highlightId(), pageable);

        List<CommentEntity> parentComments = comments.getContent();
        List<CommentDto> response = parentComments.stream()
                .map(CommentMapper.INSTANCE::toResponseCommentDto)
                .toList();

        return new ResponseCommentDto(
                response,
                comments.getTotalElements()
        );
    }
}
