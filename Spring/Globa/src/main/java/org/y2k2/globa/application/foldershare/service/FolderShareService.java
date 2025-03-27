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
import org.y2k2.globa.exception.*;
import org.y2k2.globa.infrastructure.persistence.folder.repository.FolderJpaRepository;
import org.y2k2.globa.infrastructure.persistence.folderrole.repository.FolderRoleJpaRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.repository.FolderShareJpaRepository;
import org.y2k2.globa.insfrastructure.persistence.jpa.repository.UserJpaRepository;
import org.y2k2.globa.application.foldershare.mapper.FolderShareMapper;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.common.type.FolderRole;

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

    public ResponseFolderShareUserDto getShares(Long folderId, int page, int count, UserEntity user) {
        FolderEntity folder = folderJpaRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

        if (!folder.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        }

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<FolderShareEntity> folderShareEntityPage = folderShareJpaRepository.findByFolderOrderByCreatedTimeAsc(folder, pageable);

        List<FolderShareEntity> shareEntities = folderShareEntityPage.getContent();
        Long total = folderShareEntityPage.getTotalElements();
        List<ResponseFolderShareUserDto.FolderShareUserDto> folderShareUserDtos = shareEntities.stream()
                .map(FolderShareMapper.INSTANCE::toShareUserDto)
                .collect(Collectors.toList());

        return new ResponseFolderShareUserDto(folderShareUserDtos, total);
    }

    @Transactional
    public void inviteShare(Long folderId, Long targetId, RequestInviteDto dto, UserEntity owner) {
        UserEntity target = userJpaRepository.findByUserId(targetId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_TARGET_USER));

        FolderEntity folder = folderJpaRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));
        if (!folder.getUser().getUserId().equals(owner.getUserId())) {
            throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        }

        Boolean isAlready = folderShareJpaRepository.existsByFolderAndTargetUser(folder, target);
        if (isAlready) {
            throw new CustomException(ErrorCode.SHARE_USER_DUPLICATED);
        }

        FolderRoleEntity folderRoleEntity = convertRole(FolderRole.from(dto.role()));
        FolderShareEntity folderShare = FolderShareMapper.INSTANCE.toEntity(folder, InvitationStatus.PENDING, folderRoleEntity, owner, target);
        FolderShareEntity saveFolderShare = folderShareJpaRepository.save(folderShare);

        RequestNotificationWithInvitationDto notificationInfo = RequestNotificationWithInvitationDto.builder()
                .sender(owner)
                .receiver(target)
                .folder(folder)
                .folderShare(saveFolderShare)
                .notificationType(NotificationType.SHARE_FOLDER_INVITE)
                .title("공유 초대를 보냈습니다!")
                .body(owner.getName() + "님이 " + folder.getTitle() + " 폴더를 공유하고 싶어합니다.")
                .build();

        notificationService.saveNotification(notificationInfo);
        publisher.publishEvent(notificationInfo);
    }

    @Transactional
    public void inviteShares(FolderEntity folder, UserEntity sender, List<RequestFolderPostDto.ShareTarget> shareTargets) {
        if (!folder.getUser().getUserId().equals(sender.getUserId())) throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);

        List<RequestFolderPostDto.ShareTarget> targetWithoutMe = shareTargets.stream().filter(
                shareTarget -> !shareTarget.code().equals(sender.getCode())
        ).toList();

        List<UserEntity> targets = userJpaRepository.findAllByCodeIn(
                targetWithoutMe.stream().map(RequestFolderPostDto.ShareTarget::code).toList()
        );

        if (targets.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);
        }

        FolderRoleEntity readRole = convertRole(FolderRole.READER);
        FolderRoleEntity writeRole = convertRole(FolderRole.WRITER);

        List<FolderShareEntity> newFolderShares = targets.stream().map(
                target -> {
                    String role = targetWithoutMe.stream()
                            .filter(shareTarget -> shareTarget.code().equals(target.getCode()))
                            .findFirst()
                            .map(RequestFolderPostDto.ShareTarget::role)
                            .orElse(null);

                    if (role == null) {
                        return null;
                    }

                    FolderRoleEntity folderRole = role.equalsIgnoreCase(FolderRole.WRITER.toString()) ? writeRole : readRole;
                    return FolderShareEntity.create(folder, sender, target, folderRole);
                }
        ).toList();

        List<FolderShareEntity> savedFolderShares = folderShareJpaRepository.saveAll(newFolderShares);
        List<RequestNotificationWithInvitationDto> notificationInfos = savedFolderShares.stream().map(
                folderShare -> RequestNotificationWithInvitationDto.builder()
                        .sender(sender)
                        .receiver(folderShare.getTargetUser())
                        .folder(folder)
                        .folderShare(folderShare)
                        .notificationType(NotificationType.SHARE_FOLDER_INVITE)
                        .title("공유 초대를 보냈습니다!")
                        .body(sender.getName() + "님이 " + folder.getTitle() + " 폴더를 공유하고 싶어합니다.")
                        .build()
        ).collect(Collectors.toList());

        notificationService.saveNotifications(notificationInfos);
        publisher.publishEvent(notificationInfos);
    }

    @Transactional
    public void editInviteShare(Long folderId, Long targetId, RequestInviteDto dto, UserEntity owner) {
        UserEntity targetEntity = userJpaRepository.findByUserId(targetId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_TARGET_USER));

        FolderEntity folderEntity = folderJpaRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

        FolderShareEntity folderShareEntity = folderShareJpaRepository.findByFolderAndTargetUser(folderEntity, targetEntity)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SHARE));

        checkValidation(folderShareEntity.getFolder(), owner.getUserId(), targetId);

        FolderRoleEntity folderRoleEntity = convertRole(FolderRole.from(dto.role()));
        folderShareEntity.setRole(folderRoleEntity);
        folderShareJpaRepository.save(folderShareEntity);
    }

    @Transactional
    public void deleteInviteShare(Long folderId, Long targetId, UserEntity owner) {
        UserEntity targetEntity = userJpaRepository.findByUserId(targetId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_TARGET_USER));

        FolderEntity folderEntity = folderJpaRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));;

        FolderShareEntity folderShareEntity = folderShareJpaRepository.findByFolderAndTargetUser(folderEntity, targetEntity)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SHARE));

        checkValidation(folderShareEntity.getFolder(), owner.getUserId(), targetId);
        folderShareJpaRepository.delete(folderShareEntity);
    }

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

    private void checkValidation(FolderEntity folderEntity, Long ownerId, Long targetId) {
        if (!folderEntity.getUser().getUserId().equals(ownerId)) throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        if (ownerId.equals(targetId)) throw new CustomException(ErrorCode.INVITE_BAD_REQUEST);
    }

    private FolderRoleEntity convertRole(FolderRole folderRole) {
        FolderRoleEntity folderRoleEntity;

        if (folderRole.equals(FolderRole.WRITER)) {
            folderRoleEntity = folderRoleJpaRepository.findByRoleName(FolderRole.WRITER.getRoleName());
        } else {
            folderRoleEntity = folderRoleJpaRepository.findByRoleName(FolderRole.READER.getRoleName());
        }

        return folderRoleEntity;
    }
}
