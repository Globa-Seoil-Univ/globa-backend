package org.y2k2.globa.application.foldershare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.foldershare.command.VerifyInvitationCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyInvitationUseCase;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithFolderShareDto;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcceptInvitationService {
    private final ApplicationEventPublisher publisher;

    private final VerifyInvitationUseCase verifyInvitationUseCase;
    private final CreateNotificationUseCase createNotificationUseCase;

    private final FolderShareRepository folderShareRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void accept(Long folderId, Long shareId, CustomUserDetails receiver) {
        FolderShareEntity folderShare = folderShareRepository.getShareInvitationWithFolder(folderId, receiver.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SHARE));

        verifyInvitationUseCase.execute(
                VerifyInvitationCommand.of(folderShare, folderId, shareId, receiver.getUserId())
        );

        folderShare.setInvitationStatus(InvitationStatus.ACCEPT);
        folderShareRepository.save(folderShare);

        notificationRepository.getReceivedNotification(
                folderId,
                shareId,
                receiver.getUserId()
        ).ifPresent(notificationRepository::delete);

        RequestNotificationWithFolderShareDto notification = RequestNotificationWithFolderShareDto.builder()
                .sender(folderShare.getTargetUser())
                .folder(folderShare.getFolder())
                .folderShare(folderShare)
                .notificationType(NotificationType.SHARE_FOLDER_ADD_USER)
                .build();

        createNotificationUseCase.execute(
                CreateNotificationCommand.of(notification)
        );

        sendNotification(
                receiver.getUsername(),
                folderShare.getFolder().getTitle(),
                folderId,
                receiver.getUserId()
        );
    }

    @Async
    public void sendNotification(String username, String folderTitle, Long folderId, Long excludeId) {
        List<FolderShareEntity> targetFolderShares = folderShareRepository.getAllShareInvitationsWithoutMe(folderId, excludeId);
        List<RequestNotificationWithFolderShareDto> notificationInfos = targetFolderShares.stream()
                .map(targetFolderShare -> RequestNotificationWithFolderShareDto.builder()
                        .sender(targetFolderShare.getTargetUser())
                        .folder(targetFolderShare.getFolder())
                        .folderShare(targetFolderShare)
                        .notificationType(NotificationType.SHARE_FOLDER_ADD_USER)
                        .title("새로운 사용자가 추가되었습니다!")
                        .body(username + "님이 " + folderTitle + " 폴더 공유를 수락했습니다.")
                        .build()
                ).collect(Collectors.toList());

        publisher.publishEvent(notificationInfos);
    }
}
