package org.y2k2.globa.application.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.comment.command.GetInfoForCommentCommand;
import org.y2k2.globa.application.comment.dto.common.InfoForCommentDto;
import org.y2k2.globa.application.comment.dto.request.RequestCommentDto;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.mapper.CommentMapper;
import org.y2k2.globa.application.comment.usecase.GetInfoForCommentUseCase;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderWritableUseCase;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.command.SendCommentNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithFolderShareCommentDto;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
import org.y2k2.globa.application.notification.usecase.SendCommentNotificationUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class CreateParentCommentService {
    private final FindUserUseCase findUserUseCase;
    private final GetInfoForCommentUseCase getInfoForCommentUseCase;
    private final VerifyFolderWritableUseCase verifyFolderWritableUseCase;
    private final CreateNotificationUseCase createNotificationUseCase;
    private final SendCommentNotificationUseCase sendCommentNotificationUseCase;

    private final CommentRepository commentRepository;

    @Transactional
    public void create(RequestCommentWithIdsDto idsDto, RequestCommentDto request) {
        verifyFolderWritableUseCase.execute(
                VerifyFolderCommand.of(idsDto.userId(), idsDto.folderId())
        );

        UserEntity user = findUserUseCase.execute(idsDto.userId());
        InfoForCommentDto info = getInfoForCommentUseCase.execute(
                GetInfoForCommentCommand.of(idsDto)
        );

        CommentEntity comment = CommentMapper.INSTANCE.toParentCommentEntity(
                user,
                info.highlight(),
                request.content()
        );
        CommentEntity savedComment = commentRepository.save(comment);

        saveNotification(
                user,
                info.section(),
                savedComment,
                info.folderShare()
        );

        sendCommentNotificationUseCase.execute(SendCommentNotificationCommand.of(
                user,
                info.section().getRecord().getFolder(),
                info.section().getRecord(),
                info.folderShare()
        ));
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
