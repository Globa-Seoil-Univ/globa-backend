package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.projection.NotificationProjection;
import org.y2k2.globa.projection.NotificationUnReadCount;
import org.y2k2.globa.dto.common.notification.*;
import org.y2k2.globa.dto.response.notification.ResponseNotificationDto;
import org.y2k2.globa.dto.response.notification.ResponseUnreadCountDto;
import org.y2k2.globa.dto.response.notification.ResponseUnreadNotificationDto;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.NotificationEntity;
import org.y2k2.globa.entity.NotificationReadEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.mapper.NotificationMapper;
import org.y2k2.globa.mapper.NotificationReadMapper;
import org.y2k2.globa.repository.FolderShareRepository;
import org.y2k2.globa.repository.NotificationReadRepository;
import org.y2k2.globa.repository.NotificationRepository;
import org.y2k2.globa.type.InvitationStatus;
import org.y2k2.globa.type.NotificationSort;
import org.y2k2.globa.type.NotificationType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final FolderShareRepository folderShareRepository;

    public ResponseNotificationDto getNotifications(int count, int page, NotificationSort sort, UserEntity user) {
        boolean includeNotice = false;
        boolean includeShare = false;
        boolean includeInvite = false;
        boolean includeRecord = false;
        boolean includeInquiry = false;

        switch (sort) {
            case NOTICE:
                includeNotice = true;
                break;
            case SHARE:
                includeShare = true;
                includeInvite = true;
                break;
            case INQUIRY:
                includeInquiry = true;
                break;
            case RECORD:
                includeRecord = true;
                break;
            default:
                includeNotice = true;
                includeShare = true;
                includeInvite = true;
                includeRecord = true;
                includeInquiry = true;
        }

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<NotificationProjection> notificationEntityPage = notificationRepository.findAllByReceiverOrTypeIdInOrderByCreatedTimeDesc(
                pageable,
                user.getUserId(),
                includeNotice,
                includeInvite,
                includeShare,
                includeRecord,
                includeInquiry
        );

        List<NotificationDto> dtos = notificationEntityPage.getContent().stream()
                .map(NotificationMapper.INSTANCE::toResponseNotificationDto)
                .toList();

        return new ResponseNotificationDto(dtos, notificationEntityPage.getTotalElements());
    }

    public ResponseUnreadNotificationDto getHasUnreadNotification(UserEntity user) {
        Long hasUnread = notificationRepository.existsByReceiver(user.getUserId());
        return new ResponseUnreadNotificationDto(hasUnread != 0);
    }

    public ResponseUnreadCountDto getCountUnreadNotification(UserEntity user) {
        NotificationUnReadCount notificationUnReadCount = notificationRepository.countByReceiverUserId(user.getUserId());
        Long total = notificationUnReadCount.getNoticeCount() +
                notificationUnReadCount.getInviteCount() +
                notificationUnReadCount.getShareCount() +
                notificationUnReadCount.getRecordCount() +
                notificationUnReadCount.getInquiryCount();

        return new ResponseUnreadCountDto(
                total,
                notificationUnReadCount.getNoticeCount(),
                notificationUnReadCount.getInviteCount() + notificationUnReadCount.getShareCount(),
                notificationUnReadCount.getRecordCount(),
                notificationUnReadCount.getInquiryCount()
        );
    }

    @Transactional
    public void saveNotification(SendMessage sendMessage) {
        log.info("Save Notification = {}", sendMessage);

        NotificationEntity notification = createNotification(sendMessage);

        if (notification == null) {
            log.warn("Notification is null = {}", sendMessage);
            return;
        }

        notificationRepository.save(notification);
    }

    @Transactional
    public void saveNotifications(List<? extends SendMessage> sendMessages) {
        log.info("Save Notifications count = {}", sendMessages.size());
        List<NotificationEntity> notifications = new ArrayList<>();

        for (SendMessage sendMessage : sendMessages) {
            NotificationEntity notification = createNotification(sendMessage);

            if (notification == null) {
                log.warn("Notification is null. userId = {}, name = {}", sendMessage.getReceiver().getUserId(), sendMessage.getReceiver().getName());
                continue;
            }

            notifications.add(notification);
        }

        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void readNotification(long notificationId, UserEntity user) {
        Optional<NotificationReadEntity> notificationRead = notificationReadRepository.findByNotificationNotificationId(notificationId);

        if (notificationRead.isPresent()) {
            throw new CustomException(ErrorCode.NOTIFICATION_READ_DUPLICATED);
        }

        NotificationEntity notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NOTIFICATION));

        log.info("test = {}", notification.getReceiver().getName());

        checkAccessible(notification, user);

        NotificationReadEntity readEntity = NotificationReadMapper.INSTANCE.toEntity(notification, user, false);
        notificationReadRepository.save(readEntity);
    }

    @Transactional
    public void deleteNotification(long notificationId, UserEntity user) {
        NotificationEntity notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NOTIFICATION));

        checkAccessible(notification, user);

        char type = notification.getTypeId();
        if (type == NotificationType.NOTICE.getTypeId() || isShareNotification(notification)) {
            // 공지, 공유 관련 알림은 불특정 다수에게 전달되는 알림으로 특정 사용자만 삭제한 것으로 처리
            NotificationReadEntity readEntity = notificationReadRepository.findByNotificationNotificationId(notificationId)
                    .orElseGet(() -> NotificationReadMapper.INSTANCE.toEntity(notification, user, true));

            notificationReadRepository.save(readEntity);
        } else {
            notificationRepository.delete(notification);
        }
    }

    private NotificationEntity createNotification(SendMessage sendMessage) {
        NotificationEntity notification;

        switch (sendMessage.getNotificationType()) {
            case NOTICE:
                notification = NotificationMapper.INSTANCE.toNotificationWithNotice((RequestNotificationWithTopicDto) sendMessage);
                notification.setTypeId(NotificationType.NOTICE.getTypeId());
                break;
            case SHARE_FOLDER_ADD_FILE:
                notification = NotificationMapper.INSTANCE.toNotificationWithFolderShareAddUser((RequestNotificationWithFolderShareAddUserDto) sendMessage);
                notification.setTypeId(NotificationType.SHARE_FOLDER_ADD_FILE.getTypeId());
                break;
            case SHARE_FOLDER_ADD_USER:
                notification = NotificationMapper.INSTANCE.toNotificationWithFolderShareAddUser((RequestNotificationWithFolderShareAddUserDto) sendMessage);
                notification.setTypeId(NotificationType.SHARE_FOLDER_ADD_USER.getTypeId());
                break;
            case SHARE_FOLDER_ADD_COMMENT:
                notification = NotificationMapper.INSTANCE.toNotificationWithFolderShareComment((RequestNotificationWithFolderShareCommentDto) sendMessage);
                notification.setTypeId(NotificationType.SHARE_FOLDER_ADD_COMMENT.getTypeId());
                break;
            case UPLOAD_SUCCESS:
                notification = NotificationMapper.INSTANCE.toNotificationWithNotice((RequestNotificationWithTopicDto) sendMessage);
                notification.setTypeId(NotificationType.UPLOAD_SUCCESS.getTypeId());
                break;
            case UPLOAD_FAILED:
                notification = NotificationMapper.INSTANCE.toNotificationWithNotice((RequestNotificationWithTopicDto) sendMessage);
                notification.setTypeId(NotificationType.UPLOAD_FAILED.getTypeId());
                break;
            case INQUIRY:
                notification = NotificationMapper.INSTANCE.toNotificationWithInquiry((RequestNotificationWithInquiryDto) sendMessage);
                notification.setTypeId(NotificationType.INQUIRY.getTypeId());
                break;
            case SHARE_FOLDER_INVITE:
                notification = NotificationMapper.INSTANCE.toNotificationWithInvitation((RequestNotificationWithInvitationDto) sendMessage);
                notification.setTypeId(NotificationType.SHARE_FOLDER_INVITE.getTypeId());
                break;
            default:
                return null;
        }

        return notification;
    }

    private boolean isAccessibleNotification(NotificationEntity notification, UserEntity user) {
        return notification.getReceiver().getUserId().equals(user.getUserId());
    }

    private boolean isShareNotification(NotificationEntity notification) {
        return notification.getTypeId() == NotificationType.SHARE_FOLDER_ADD_COMMENT.getTypeId() ||
                notification.getTypeId() == NotificationType.SHARE_FOLDER_ADD_FILE.getTypeId() ||
                notification.getTypeId() == NotificationType.SHARE_FOLDER_ADD_USER.getTypeId();
    }

    private boolean isAccessibleShareNotification(NotificationEntity notification, UserEntity user) {
        if (notification.getFolder() == null || notification.getFolder().getFolderId() == null) {
            return false;
        }

        FolderShareEntity folderShare = folderShareRepository.findByFolderAndTargetUser(notification.getFolder(), user)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER));

        return folderShare.getInvitationStatus().equals(InvitationStatus.ACCEPT);
    }

    private void checkAccessible(NotificationEntity notification, UserEntity user) {
        if (!isAccessibleNotification(notification, user)) {
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION);
        }

        if (isShareNotification(notification) && !isAccessibleShareNotification(notification, user)) {
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION);
        }
    }
}
