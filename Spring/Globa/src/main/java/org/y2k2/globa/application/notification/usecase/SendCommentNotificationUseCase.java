package org.y2k2.globa.application.notification.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.notification.command.SendCommentNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithFolderShareCommentDto;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SendCommentNotificationUseCase implements VoidUseCase<SendCommentNotificationCommand> {
    private final ApplicationEventPublisher publisher;

    private final FolderShareRepository folderShareRepository;

    @Override
    public void execute(SendCommentNotificationCommand command) {
        List<FolderShareEntity> targetFolderShares = folderShareRepository.getAllShareInvitations(command.folder().getFolderId());
        List<RequestNotificationWithFolderShareCommentDto> infos = targetFolderShares.stream()
                .map(targetFolderShare -> RequestNotificationWithFolderShareCommentDto.builder()
                        .sender(command.user())
                        .title(command.user().getName() + "님이 댓글을 달았습니다!")
                        .folder(command.folder())
                        .folderShare(targetFolderShare)
                        .record(command.record())
                        .notificationType(NotificationType.SHARE_FOLDER_ADD_COMMENT)
                        .build())
                .collect(Collectors.toList());

        publisher.publishEvent(infos);
    }
}
