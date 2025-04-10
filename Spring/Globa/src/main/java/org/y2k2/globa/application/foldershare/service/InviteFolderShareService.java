package org.y2k2.globa.application.foldershare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.folderrole.command.GetFolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.GetFolderRoleUseCase;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.dto.request.RequestInviteDto;
import org.y2k2.globa.application.foldershare.mapper.FolderShareMapper;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithInvitationDto;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.type.FolderRole;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InviteFolderShareService {
    private final ApplicationEventPublisher publisher;

    private final FindUserUseCase findUserUseCase;
    private final GetFolderRoleUseCase getFolderRoleUseCase;
    private final CreateNotificationUseCase createNotificationUseCase;
    private final VerifyFolderOwnerUseCase verifyFolderOwnerUseCase;

    private final FolderRepository folderRepository;
    private final FolderShareRepository folderShareRepository;

    @Transactional
    public void invite(Long folderId, Long targetId, RequestInviteDto dto, Long ownerId) {
        UserEntity owner = findUserUseCase.execute(ownerId);
        UserEntity target = findUserUseCase.execute(targetId);
        FolderEntity folder = folderRepository.getFolder(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));
        verifyFolderOwnerUseCase.execute(
                VerifyFolderCommand.of(ownerId, folderId)
        );

        Boolean isInvited = folderShareRepository.isInvited(folderId, target);
        if (isInvited) {
            throw new CustomException(ErrorCode.SHARE_USER_DUPLICATED);
        }

        FolderRoleEntity folderRole = getFolderRoleUseCase.execute(
                GetFolderRoleCommand.of(FolderRole.from(dto.role()))
        );
        FolderShareEntity folderShare = FolderShareMapper.INSTANCE.toEntity(
                folder,
                InvitationStatus.PENDING,
                folderRole,
                owner,
                target
        );
        FolderShareEntity savedFolderShare = folderShareRepository.save(folderShare);

        RequestNotificationWithInvitationDto notificationInfo = RequestNotificationWithInvitationDto.builder()
                .sender(owner)
                .receiver(target)
                .folder(folder)
                .folderShare(savedFolderShare)
                .notificationType(NotificationType.SHARE_FOLDER_INVITE)
                .title("공유 초대를 보냈습니다!")
                .body(owner.getName() + "님이 " + folder.getTitle() + " 폴더를 공유하고 싶어합니다.")
                .build();

        createNotificationUseCase.execute(
                CreateNotificationCommand.of(notificationInfo)
        );

        publisher.publishEvent(notificationInfo);
    }
}
