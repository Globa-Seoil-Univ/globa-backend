package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.Projection.NotificationProjection;
import org.y2k2.globa.Projection.NotificationUnReadCount;
import org.y2k2.globa.dto.NotificationDto;
import org.y2k2.globa.dto.ResponseNotificationDto;
import org.y2k2.globa.dto.ResponseUnreadCountDto;
import org.y2k2.globa.dto.ResponseUnreadNotificationDto;
import org.y2k2.globa.entity.NotificationEntity;
import org.y2k2.globa.entity.NotificationReadEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.mapper.NotificationMapper;
import org.y2k2.globa.repository.NotificationReadRepository;
import org.y2k2.globa.repository.NotificationRepository;
import org.y2k2.globa.repository.UserRepository;
import org.y2k2.globa.type.NotificationSort;
import org.y2k2.globa.type.NotificationType;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final UserRepository userRepository;

    public ResponseNotificationDto getNotifications(long userId, int count, int page, NotificationSort sort) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);
        System.out.println(userId);

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
        Page<NotificationProjection> notificationEntityPage = notificationRepository.findAllByToUserOrTypeIdInOrderByCreatedTimeDesc(
                pageable,
                user.getUserId(),
                includeNotice,
                includeInvite,
                includeShare,
                includeRecord,
                includeInquiry
        );
        List<NotificationProjection> notifications = notificationEntityPage.getContent();
        long total = notificationEntityPage.getTotalElements();

        List<NotificationDto> dtos = notifications.stream()
                .map(NotificationMapper.INSTANCE::toResponseNotificationDto)
                .toList();

        return new ResponseNotificationDto(dtos, total);
    }

    public ResponseUnreadNotificationDto getHasUnreadNotification(long userId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        Long hasUnread = notificationRepository.existsByToUser(userId);
        return new ResponseUnreadNotificationDto(hasUnread != 0);
    }

//    public ResponseNotificationDto getUnreadNotification(long userId, String type) {
//        UserEntity user = userRepository.findByUserId(userId);
//        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
//        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);
//
//        List<NotificationEntity> notificationEntities = notificationRepository.findAllByToUserUserId(userId);
//
//        List<Long> notificationIds = new ArrayList<>();
//
//        for (NotificationEntity notification : notificationEntities) {
//            NotificationReadEntity readEntity = notificationReadRepository.findByNotificationNotificationId(notification.getNotificationId());
//            if (readEntity == null)
//                notificationIds.add(notification.getNotificationId());
//        }
//
//        switch (type)
//        {
//            case "a" :
//                notificationEntities = notificationRepository.findAllByNotificationIdInAndTypeIdIn(notificationIds,TYPE_ALL);
//                break;
//            case "n" :
//                notificationEntities = notificationRepository.findAllByNotificationIdInAndTypeIdIn(notificationIds,TYPE_NOTICE);
//                break;
//            case "s" :
//                notificationEntities = notificationRepository.findAllByNotificationIdInAndTypeIdIn(notificationIds,TYPE_SHARE);
//                break;
//            case "r" :
//                notificationEntities = notificationRepository.findAllByNotificationIdInAndTypeIdIn(notificationIds,TYPE_RECORD);
//                break;
//            default:
//                throw new CustomException(ErrorCode.NOFI_TYPE_BAD_REQUEST);
//        }
//
//        List<NotificationDto> dtos = notificationEntities.stream()
//                .map(NotificationMapper.INSTANCE::toResponseNotificationDto)
//                .toList();
//        long total = notificationEntities.size();
//
//        return new ResponseNotificationDto(dtos, total);
//    }

    public ResponseUnreadCountDto getCountUnreadNotification(long userId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        NotificationUnReadCount notificationUnReadCount = notificationRepository.countByToUserUserId(userId);
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

    public void postNotificationRead(long userId, long notificationId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        NotificationReadEntity notificationRead = notificationReadRepository.findByNotificationNotificationId(notificationId);
        if(notificationRead != null) throw new CustomException(ErrorCode.NOTIFICATION_READ_DUPLICATED);

        NotificationEntity notification = notificationRepository.findByNotificationId(notificationId);
        if(notification == null) throw new CustomException(ErrorCode.NOT_FOUND_NOTIFICATION);

        NotificationReadEntity readEntity = new NotificationReadEntity();
        readEntity.setNotification(notification);
        readEntity.setUser(user);
        readEntity.setIsDeleted(false);

        notificationReadRepository.save(readEntity);

    }

    public void deleteNotification(long userId, long notificationId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        NotificationEntity notification = notificationRepository.findByNotificationId(notificationId);
        if(notification == null) throw new CustomException(ErrorCode.NOT_FOUND_NOTIFICATION);

        char type = notification.getTypeId();
        if (
                type == NotificationType.NOTICE.getTypeId() ||
                type == NotificationType.SHARE_FOLDER_ADD_COMMENT.getTypeId() ||
                type == NotificationType.SHARE_FOLDER_ADD_FILE.getTypeId() ||
                type == NotificationType.SHARE_FOLDER_ADD_USER.getTypeId()
        ) {
            NotificationReadEntity readEntity = notificationReadRepository.findByNotificationNotificationId(notificationId);

            if (readEntity == null) {
                NotificationReadEntity newReadEntity = NotificationReadEntity
                                                        .builder()
                                                            .notification(notification)
                                                            .user(user)
                                                            .isDeleted(true)
                                                        .build();

                notificationReadRepository.save(newReadEntity);
            } else {
                readEntity.setIsDeleted(true);
                notificationReadRepository.save(readEntity);
            }
        } else {
            notificationRepository.delete(notification);
        }
    }

}
