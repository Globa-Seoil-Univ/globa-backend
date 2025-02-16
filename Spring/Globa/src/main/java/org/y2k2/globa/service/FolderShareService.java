package org.y2k2.globa.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.common.fcm.SendMessage;
import org.y2k2.globa.dto.request.folder.RequestFolderPostDto;
import org.y2k2.globa.dto.response.foldershare.ResponseFolderShareUserDto;
import org.y2k2.globa.dto.request.notification.RequestNotificationWithFolderShareAddUserDto;
import org.y2k2.globa.dto.request.notification.RequestNotificationWithInvitationDto;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.event.NotificationListener;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.mapper.FolderShareMapper;
import org.y2k2.globa.mapper.NotificationMapper;
import org.y2k2.globa.type.InvitationStatus;
import org.y2k2.globa.type.NotificationType;
import org.y2k2.globa.type.Role;
import org.y2k2.globa.util.CustomTimestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderShareService {
    private final ApplicationEventPublisher publisher;

    private final NotificationService notificationService;

    private final FolderShareRepository folderShareRepository;
    private final FolderRepository folderRepository;
    private final FolderRoleRepository folderRoleRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public ResponseFolderShareUserDto getShares(Long folderId, Long userId, int page, int count) {
        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));;

        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getIsDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        if (!folderEntity.getUser().getUserId().equals(userId)) throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<FolderShareEntity> folderShareEntityPage = folderShareRepository.findByFolderOrderByCreatedTimeAsc(pageable, folderEntity);

        List<FolderShareEntity> shareEntities = folderShareEntityPage.getContent();
        Long total = folderShareEntityPage.getTotalElements();
        List<ResponseFolderShareUserDto.FolderShareUserDto> folderShareUserDtos = shareEntities.stream()
                .map(FolderShareMapper.INSTANCE::toShareUserDto)
                .collect(Collectors.toList());

        return new ResponseFolderShareUserDto(folderShareUserDtos, total);
    }

    @Transactional
    public void inviteShare(Long folderId, Long ownerId, Long targetId, Role role) {
        UserEntity ownerEntity = userRepository.findByUserId(ownerId);
        UserEntity targetEntity = userRepository.findByUserId(targetId);

        if (ownerEntity.getIsDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        if (ownerId.equals(targetId)) throw new CustomException(ErrorCode.INVITE_BAD_REQUEST);
        if (targetEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));;
        if (!folderEntity.getUser().getUserId().equals(ownerId)) throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);

        FolderShareEntity folderShareEntity = folderShareRepository.findByFolderAndTargetUser(folderEntity, targetEntity);
        if (folderShareEntity != null) throw new CustomException(ErrorCode.SHARE_USER_DUPLICATED);

        FolderRoleEntity folderRoleEntity = convertRole(role);
        FolderShareEntity entity = FolderShareEntity.create(folderEntity, ownerEntity, targetEntity, folderRoleEntity);
        FolderShareEntity saveFolderShare = folderShareRepository.save(entity);

        RequestNotificationWithInvitationDto notificationInfo = RequestNotificationWithInvitationDto.builder()
                .sender(ownerEntity)
                .receiver(targetEntity)
                .folder(folderEntity)
                .folderShare(saveFolderShare)
                .notificationType(NotificationType.SHARE_FOLDER_INVITE)
                .title("공유 초대를 보냈습니다!")
                .body(ownerEntity.getName() + "님이 " + folderEntity.getTitle() + " 폴더를 공유하고 싶어합니다.")
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

        List<UserEntity> targets = userRepository.findAllByCodeIn(
                targetWithoutMe.stream().map(RequestFolderPostDto.ShareTarget::code).toList()
        );

        if (targets.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);
        }

        List<FolderShareEntity> alreadyTargets = folderShareRepository.findAllByFolderAndTargetUser_CodeIn(
                folder,
                targetWithoutMe.stream().map(RequestFolderPostDto.ShareTarget::code).toList()
        );

        if (!alreadyTargets.isEmpty()) {
            for (FolderShareEntity alreadyTarget : alreadyTargets) {
                alreadyTarget.setCreatedTime(new CustomTimestamp().getTimestamp());
            }

            folderShareRepository.saveAll(alreadyTargets);
            // TODO : 이미 초대된 사용자에게 다시 초대 보낼 때 알림 보내기
        }

        List<UserEntity> newTargets = targets.stream()
                .filter(shareTarget -> alreadyTargets.stream().noneMatch(
                        alreadyTarget -> alreadyTarget.getTargetUser().getCode().equals(shareTarget.getCode())
                ))
                .toList();

        FolderRoleEntity readRole = convertRole(Role.R);
        FolderRoleEntity writeRole = convertRole(Role.W);

        List<FolderShareEntity> newFolderShares = newTargets.stream().map(
                target -> {
                    String role = targetWithoutMe.stream()
                            .filter(shareTarget -> shareTarget.code().equals(target.getCode()))
                            .findFirst()
                            .map(RequestFolderPostDto.ShareTarget::role)
                            .orElse(null);

                    if (role == null) {
                        return null;
                    }

                    FolderRoleEntity folderRole = role.equalsIgnoreCase(Role.W.toString()) ? writeRole : readRole;

                    return FolderShareEntity.create(folder, sender, target, folderRole);
                }
        ).toList();

        List<FolderShareEntity> savedFolderShares = folderShareRepository.saveAll(newFolderShares);
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
    public void editInviteShare(Long folderId, Long ownerId, Long targetId, Role role) {
        UserEntity targetEntity = userRepository.findByUserId(targetId);

        UserEntity user = userRepository.findByUserId(ownerId);
        if (user.getIsDeleted()) throw new CustomException(ErrorCode.DELETED_USER);
        if (targetEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

        FolderShareEntity folderShareEntity = folderShareRepository.findByFolderAndTargetUser(folderEntity, targetEntity);
        if (folderShareEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_SHARE);

        checkValidation(folderShareEntity.getFolder(), ownerId, targetId, targetEntity);

        FolderRoleEntity folderRoleEntity = convertRole(role);
        folderShareEntity.setRole(folderRoleEntity);
        folderShareRepository.save(folderShareEntity);
    }

    @Transactional
    public void deleteInviteShare(Long folderId, Long ownerId, Long targetId) {
        UserEntity user = userRepository.findByUserId(ownerId);
        if (user.getIsDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        UserEntity targetEntity = userRepository.findByUserId(targetId);
        if (targetEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));;

        FolderShareEntity folderShareEntity = folderShareRepository.findByFolderAndTargetUser(folderEntity, targetEntity);
        if (folderShareEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_SHARE);

        checkValidation(folderShareEntity.getFolder(), ownerId, targetId, targetEntity);

        folderShareRepository.delete(folderShareEntity);
    }

    @Transactional
    public void acceptShare(Long folderId, Long shareId, Long targetId) {
        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByShareId(shareId);
        checkValidation(folderShareEntity, folderId, targetId);

        folderShareEntity.setInvitationStatus(InvitationStatus.ACCEPT);
        folderShareRepository.save(folderShareEntity);

        NotificationEntity invitationNotification = notificationRepository.findByFolderFolderIdAndFolderShareShareIdAndReceiverUserId(folderId, shareId, targetId);
        if (invitationNotification != null) {
            notificationRepository.delete(invitationNotification);
        }

        RequestNotificationWithFolderShareAddUserDto notification = RequestNotificationWithFolderShareAddUserDto.builder()
                .receiver(folderShareEntity.getTargetUser())
                .folder(folderShareEntity.getFolder())
                .folderShare(folderShareEntity)
                .notificationType(NotificationType.SHARE_FOLDER_ADD_USER)
                .build();
        notificationService.saveNotification(notification);

        List<FolderShareEntity> targetFolderShares = folderShareRepository.findAllByFolderFolderId(folderId);
        List<RequestNotificationWithFolderShareAddUserDto> notificationInfos = targetFolderShares.stream()
                .map(targetFolderShare -> RequestNotificationWithFolderShareAddUserDto.builder()
                        .receiver(targetFolderShare.getTargetUser())
                        .folder(targetFolderShare.getFolder())
                        .folderShare(targetFolderShare)
                        .notificationType(NotificationType.SHARE_FOLDER_ADD_USER)
                        .title("새로운 사용자가 추가되었습니다!")
                        .body(folderShareEntity.getTargetUser().getName() + "님이 " + folderShareEntity.getFolder().getTitle() + " 폴더 공유를 수락했습니다.")
                        .build()
                ).collect(Collectors.toList());

        publisher.publishEvent(notificationInfos);
    }
    @Transactional
    public void refuseShare(Long folderId, Long shareId, Long targetId) {
        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByShareId(shareId);
        checkValidation(folderShareEntity, folderId, targetId);
        folderShareRepository.delete(folderShareEntity);
    }

    private void checkValidation(FolderShareEntity folderShareEntity, Long folderId, Long targetId) {
        if (folderShareEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_SHARE);
        if (!folderShareEntity.getFolder().getFolderId().equals(folderId)) throw new CustomException(ErrorCode.MISMATCH_FOLDER_ID);
        if (folderShareEntity.getInvitationStatus().equals(String.valueOf(InvitationStatus.ACCEPT))) throw new CustomException(ErrorCode.INVITE_ACCEPT_BAD_REQUEST);

        UserEntity targetEntity = folderShareEntity.getTargetUser();
        if (targetEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);
        if (!targetEntity.getUserId().equals(targetId)) throw new CustomException(ErrorCode.NOT_DESERVE_MODIFY_INVITATION);
        if (targetEntity.getIsDeleted()) throw new CustomException(ErrorCode.DELETED_USER);
    }

    private void checkValidation(FolderEntity folderEntity, Long ownerId, Long targetId, UserEntity targetEntity) {
        if (ownerId.equals(targetId)) throw new CustomException(ErrorCode.INVITE_BAD_REQUEST);
        if (folderEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_FOLDER);
        if (!folderEntity.getUser().getUserId().equals(ownerId)) throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        if (targetEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);
    }

    private FolderRoleEntity convertRole(Role role) {
        FolderRoleEntity folderRoleEntity;

        if (role.equals(Role.W)) {
            folderRoleEntity = folderRoleRepository.findByRoleName(Role.W.getRoleName());
        } else {
            folderRoleEntity = folderRoleRepository.findByRoleName(Role.R.getRoleName());
        }

        return folderRoleEntity;
    }
}
