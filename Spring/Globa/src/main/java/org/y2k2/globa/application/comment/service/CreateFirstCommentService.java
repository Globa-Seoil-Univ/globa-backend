package org.y2k2.globa.application.comment.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.hpsf.Section;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.dto.request.RequestFirstCommentDto;
import org.y2k2.globa.application.comment.mapper.CommentMapper;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.hightlight.mapper.HighlightMapper;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.command.SendCommentNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithFolderShareCommentDto;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
import org.y2k2.globa.application.notification.usecase.SendCommentNotificationUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.domain.section.repository.SectionRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class CreateFirstCommentService {
    private final VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    private final FindUserUseCase findUserUseCase;
    private final CreateNotificationUseCase createNotificationUseCase;
    private final SendCommentNotificationUseCase sendCommentNotificationUseCase;

    private final FolderShareRepository folderShareRepository;
    private final SectionRepository sectionRepository;
    private final CommentRepository commentRepository;
    private final HighlightRepository highlightRepository;

    @Transactional
    public long create(RequestCommentWithIdsDto idsDto, RequestFirstCommentDto request) {
        verifyFolderAccessibleUseCase.execute(
                VerifyFolderCommand.of(idsDto.userId(), idsDto.folderId())
        );

        UserEntity user = findUserUseCase.execute(idsDto.userId());

        boolean isAlreadyHighlighted = highlightRepository.hasHighlightInRange(
                idsDto.sectionId(),
                request.startIdx(),
                request.endIdx()
        );
        if (isAlreadyHighlighted) {
            throw new CustomException(ErrorCode.HIGHLIGHT_DUPLICATED);
        }

        SectionEntity section = sectionRepository.getSectionAllJoin(idsDto.sectionId(), idsDto.folderId(), idsDto.recordId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SECTION));
        HighlightEntity highlight = HighlightMapper.INSTANCE.toEntity(section, request.startIdx(), request.endIdx());
        HighlightEntity savedHighlight = highlightRepository.save(highlight);

        CommentEntity comment = CommentMapper.INSTANCE.toParentCommentEntity(user, savedHighlight, request.content());
        commentRepository.save(comment);

        FolderShareEntity folderShare = folderShareRepository.getShareInvitation(idsDto.folderId(), idsDto.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SHARE));

        saveNotification(user, section, comment, folderShare);
        sendCommentNotificationUseCase.execute(SendCommentNotificationCommand.of(
                user,
                section.getRecord().getFolder(),
                section.getRecord(),
                folderShare
        ));

        return savedHighlight.getHighlightId();
    }

    private void saveNotification(
            UserEntity sender,
            SectionEntity section,
            CommentEntity comment,
            FolderShareEntity folderShare
    ) {
        RequestNotificationWithFolderShareCommentDto info = RequestNotificationWithFolderShareCommentDto.builder()
                .sender(sender)
                .title(sender.getName() + "님이 댓글을 달았습니다!")
                .folder(section.getRecord().getFolder())
                .folderShare(folderShare)
                .record(section.getRecord())
                .comment(comment)
                .notificationType(NotificationType.SHARE_FOLDER_ADD_COMMENT)
                .build();

        createNotificationUseCase.execute(
                CreateNotificationCommand.of(info)
        );
    }
}
