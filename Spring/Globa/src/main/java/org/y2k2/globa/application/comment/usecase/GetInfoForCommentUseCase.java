package org.y2k2.globa.application.comment.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.comment.command.GetInfoForCommentCommand;
import org.y2k2.globa.application.comment.dto.common.InfoForCommentDto;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.domain.section.repository.SectionRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

@Component
@RequiredArgsConstructor
public class GetInfoForCommentUseCase implements UseCase<GetInfoForCommentCommand, InfoForCommentDto> {
    private final FolderShareRepository folderShareRepository;
    private final SectionRepository sectionRepository;
    private final HighlightRepository highlightRepository;

    @Override
    public InfoForCommentDto execute(GetInfoForCommentCommand command) {
        RequestCommentWithIdsDto idsDto = command.dto();

        SectionEntity section = sectionRepository.getSectionJoinFolderAndRecord(idsDto.sectionId(), idsDto.folderId(), idsDto.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));

        HighlightEntity highlight = highlightRepository.getHighlight(idsDto.sectionId(), idsDto.highlightId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_HIGHLIGHT));

        FolderShareEntity folderShare = folderShareRepository.getShareInvitation(idsDto.folderId(), idsDto.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SHARE));

        return InfoForCommentDto.of(section, highlight, folderShare);
    }
}
