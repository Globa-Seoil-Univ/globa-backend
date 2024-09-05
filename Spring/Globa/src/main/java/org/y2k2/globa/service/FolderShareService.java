package org.y2k2.globa.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.mapper.FolderShareMapper;
import org.y2k2.globa.mapper.NotificationMapper;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.type.InvitationStatus;
import org.y2k2.globa.type.NotificationType;
import org.y2k2.globa.type.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderShareService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final FirebaseMessaging firebaseMessaging;
    private final FolderShareRepository folderShareRepository;
    private final FolderRepository folderRepository;
    private final FolderRoleRepository folderRoleRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public ResponseFolderShareUserDto getShares(Long folderId, Long userId, int page, int count) {
        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId);

        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        if (folderEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_FOLDER);
        if (!folderEntity.getUser().getUserId().equals(userId)) throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<FolderShareEntity> folderShareEntityPage = folderShareRepository.findByFolderOrderByCreatedTimeAsc(pageable, folderEntity);

        List<FolderShareEntity> shareEntities = folderShareEntityPage.getContent();
        Long total = folderShareEntityPage.getTotalElements();
        List<FolderShareUserDto> folderShareUserDtos = shareEntities.stream()
                .map(FolderShareMapper.INSTANCE::toShareUserDto)
                .collect(Collectors.toList());

        return new ResponseFolderShareUserDto(folderShareUserDtos, total);
    }

    @Transactional
    public void inviteShare(Long folderId, Long ownerId, Long targetId, Role role) {
        UserEntity ownerEntity = userRepository.findByUserId(ownerId);
        UserEntity targetEntity = userRepository.findByUserId(targetId);

        if (ownerEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        if (ownerId.equals(targetId)) throw new CustomException(ErrorCode.INVITE_BAD_REQUEST);
        if (targetEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId);
        if (folderEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_FOLDER);
        if (!folderEntity.getUser().getUserId().equals(ownerId)) throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);

        FolderShareEntity folderShareEntity = folderShareRepository.findByFolderAndTargetUser(folderEntity, targetEntity);
        if (folderShareEntity != null) throw new CustomException(ErrorCode.SHARE_USER_DUPLICATED);

        FolderRoleEntity folderRoleEntity = convertRole(role);
        FolderShareEntity entity = FolderShareEntity.create(folderEntity, ownerEntity, targetEntity, folderRoleEntity);
        FolderShareEntity saveFolderShare = folderShareRepository.save(entity);

        NotificationEntity notification = NotificationMapper.INSTANCE.toNotificationWithInvitation(
                new RequestNotificationWithInvitationDto(ownerEntity, targetEntity, folderEntity, saveFolderShare)
        );
        notification.setTypeId(NotificationType.SHARE_FOLDER_INVITE.getTypeId());
        notificationRepository.save(notification);

        if (!targetEntity.getShareNofi() || targetEntity.getNotificationToken() == null) return;

        try {
            Message message = Message.builder()
                    .setToken(targetEntity.getNotificationToken())
                    .setNotification(Notification.builder()
                            .setTitle("공유 초대를 보냈습니다!")
                            .setBody(ownerEntity.getName() + "님이 " + folderEntity.getTitle() + " 폴더를 공유하고 싶어합니다.")
                            .build())
                    .build();

            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT || e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                targetEntity.setNotificationToken(null);
                targetEntity.setNotificationTokenTime(null);
                userRepository.save(targetEntity);
                log.debug("Delete Notification Token : " + targetEntity.getUserId());
            }

            log.debug("Failed to send invite notification : " + saveFolderShare.getShareId() + " : " + e.getMessage());
        } catch (Exception e) {
            log.debug("Failed to send invite notification : " + saveFolderShare.getShareId() + " : " + e.getMessage());
        }
    }

    @Transactional
    public void editInviteShare(Long folderId, Long ownerId, Long targetId, Role role) {
        UserEntity targetEntity = userRepository.findByUserId(targetId);

        UserEntity user = userRepository.findByUserId(ownerId);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);
        if (targetEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId);
        if (folderEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_FOLDER);

        FolderShareEntity folderShareEntity = folderShareRepository.findByFolderAndTargetUser(folderEntity, targetEntity);
        if (folderShareEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_SHARE);

        checkValidation(folderShareEntity.getFolder(), ownerId, targetId, targetEntity);

        FolderRoleEntity folderRoleEntity = convertRole(role);
        folderShareEntity.setRoleId(folderRoleEntity);
        folderShareRepository.save(folderShareEntity);
    }

    @Transactional
    public void deleteInviteShare(Long folderId, Long ownerId, Long targetId) {
        UserEntity user = userRepository.findByUserId(ownerId);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        UserEntity targetEntity = userRepository.findByUserId(targetId);
        if (targetEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId);
        if (folderEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_FOLDER);

        FolderShareEntity folderShareEntity = folderShareRepository.findByFolderAndTargetUser(folderEntity, targetEntity);
        if (folderShareEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_SHARE);

        checkValidation(folderShareEntity.getFolder(), ownerId, targetId, targetEntity);

        folderShareRepository.delete(folderShareEntity);
    }

    @Transactional
    public void acceptShare(Long folderId, Long shareId, Long targetId) {
        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByShareId(shareId);
        checkValidation(folderShareEntity, folderId, targetId);

        folderShareEntity.setInvitationStatus(String.valueOf(InvitationStatus.ACCEPT));
        folderShareRepository.save(folderShareEntity);

        NotificationEntity invitationNotification = notificationRepository.findByFolderFolderIdAndFolderShareShareIdAndToUserUserId(folderId, shareId, targetId);
        if (invitationNotification != null) {
            notificationRepository.delete(invitationNotification);
        }

        NotificationEntity notification = NotificationMapper.INSTANCE.toNotificationWithFolderShareAddUser(
                new RequestNotificationWithFolderShareAddUserDto(folderShareEntity.getTargetUser(), folderShareEntity.getFolder(), folderShareEntity)
        );
        notification.setTypeId(NotificationType.SHARE_FOLDER_ADD_USER.getTypeId());
        notificationRepository.save(notification);

        List<FolderShareEntity> targetFolderShares = folderShareRepository.findAllByFolderFolderId(folderId);
        List<Message> messages = new ArrayList<>();

        try {
            for (FolderShareEntity targetFolderShare : targetFolderShares) {
                boolean isNotTarget = !targetFolderShare.getTargetUser().getShareNofi()
                        || targetFolderShare.getTargetUser().getNotificationToken() == null
                        || targetFolderShare.getTargetUser().getUserId().equals(targetId);
                if (isNotTarget) {
                    continue;
                }

                Message message = Message.builder()
                        .setToken(targetFolderShare.getTargetUser().getNotificationToken())
                        .setNotification(Notification.builder()
                                .setTitle("새로운 사용자가 추가되었습니다!")
                                .setBody(folderShareEntity.getTargetUser().getName() + "님이 " + folderShareEntity.getFolder().getTitle() + " 폴더 공유를 수락했습니다.")
                                .build())
                        .build();
                messages.add(message);
            }

            firebaseMessaging.sendEach(messages, false);
        }  catch (Exception e) {
            log.debug("Failed to send invite notification : " + folderShareEntity.getShareId() + " : " + e.getMessage());
        }
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
        if (targetEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);
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
            folderRoleEntity = folderRoleRepository.findByRoleName("편집자");
        } else {
            folderRoleEntity = folderRoleRepository.findByRoleName("뷰어");
        }

        return folderRoleEntity;
    }
}
