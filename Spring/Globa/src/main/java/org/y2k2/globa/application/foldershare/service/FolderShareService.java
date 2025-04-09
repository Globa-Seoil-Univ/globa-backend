package org.y2k2.globa.application.foldershare.service;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.notification.service.NotificationService;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.notification.repository.NotificationJpaRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.application.folder.dto.request.RequestFolderPostDto;
import org.y2k2.globa.application.foldershare.dto.request.RequestInviteDto;
import org.y2k2.globa.application.foldershare.dto.response.ResponseFolderShareUserDto;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithFolderShareAddUserDto;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithInvitationDto;
import org.y2k2.globa.infrastructure.persistence.folder.repository.FolderJpaRepository;
import org.y2k2.globa.infrastructure.persistence.folderrole.repository.FolderRoleJpaRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.repository.FolderShareJpaRepository;
import org.y2k2.globa.application.foldershare.mapper.FolderShareMapper;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.common.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.user.repository.UserJpaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderShareService {
    private final ApplicationEventPublisher publisher;

    private final NotificationService notificationService;

    private final FolderShareJpaRepository folderShareJpaRepository;
    private final FolderJpaRepository folderJpaRepository;
    private final FolderRoleJpaRepository folderRoleJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final NotificationJpaRepository notificationJpaRepository;

    @Transactional
    public void acceptShare(Long folderId, Long shareId, UserEntity receiver) {
        FolderShareEntity folderShare = folderShareJpaRepository.findFirstByShareId(shareId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SHARE));
        checkValidation(folderShare, folderId, receiver.getUserId());

        folderShare.setInvitationStatus(InvitationStatus.ACCEPT);
        folderShareJpaRepository.save(folderShare);

        notificationJpaRepository.findByFolderFolderIdAndFolderShareShareIdAndReceiver(
                folderId,
                shareId,
                receiver
        ).ifPresent(notificationJpaRepository::delete);

        RequestNotificationWithFolderShareAddUserDto notification = RequestNotificationWithFolderShareAddUserDto.builder()
                .sender(folderShare.getTargetUser())
                .folder(folderShare.getFolder())
                .folderShare(folderShare)
                .notificationType(NotificationType.SHARE_FOLDER_ADD_USER)
                .build();
        notificationService.saveNotification(notification);

        sendNotification(receiver.getName(), folderShare.getFolder().getTitle(), folderId, receiver.getUserId());
    }
    @Transactional
    public void refuseShare(Long folderId, Long shareId, UserEntity user) {
        FolderShareEntity folderShare = folderShareJpaRepository.findFirstByShareId(shareId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SHARE));
        checkValidation(folderShare, folderId, user.getUserId());

        folderShareJpaRepository.delete(folderShare);
    }

    @Async
    public void sendNotification(String username, String folderTitle, Long folderId, Long excludeId) {
        List<FolderShareEntity> targetFolderShares = folderShareJpaRepository.findAllByFolderFolderIdAndTargetUser_UserIdNot(folderId, excludeId);
        List<RequestNotificationWithFolderShareAddUserDto> notificationInfos = targetFolderShares.stream()
                .map(targetFolderShare -> RequestNotificationWithFolderShareAddUserDto.builder()
                        .receiver(targetFolderShare.getTargetUser())
                        .folder(targetFolderShare.getFolder())
                        .folderShare(targetFolderShare)
                        .notificationType(NotificationType.SHARE_FOLDER_ADD_USER)
                        .title("새로운 사용자가 추가되었습니다!")
                        .body(username + "님이 " + folderTitle + " 폴더 공유를 수락했습니다.")
                        .build()
                ).collect(Collectors.toList());

        publisher.publishEvent(notificationInfos);
    }

    private void checkValidation(FolderShareEntity folderShare, Long folderId, Long targetId) {
        if (!folderShare.getTargetUser().getUserId().equals(targetId)) throw new CustomException(ErrorCode.NOT_DESERVE_ACCEPT_INVITATION);
        if (!folderShare.getFolder().getFolderId().equals(folderId)) throw new CustomException(ErrorCode.MISMATCH_FOLDER_ID);
        if (folderShare.getInvitationStatus().equals(InvitationStatus.ACCEPT)) throw new CustomException(ErrorCode.INVITE_ACCEPT_BAD_REQUEST);
    }
}
