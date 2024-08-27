package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final UserRepository userRepository;

    // a전체 n공지 s공유 r문서
    private final List<String> TYPE_ALL = new ArrayList<>(Arrays.asList("1","2","3","4","5","6","7","8"));
    private final List<String> TYPE_NOTICE = new ArrayList<>(Arrays.asList("1"));
    private final List<String> TYPE_SHARE = new ArrayList<>(Arrays.asList("2","3","4","5"));
    private final List<String> TYPE_RECORD = new ArrayList<>(Arrays.asList("6","7"));

    public ResponseNotificationDto getNotifications(long userId, int count, int page) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<NotificationEntity> notificationEntityPage = notificationRepository.findAllByToUserOrTypeIdInOrderByCreatedTimeDesc(pageable, user, new char[]{'1'});
        List<NotificationEntity> tempNotifications = notificationEntityPage.getContent();
        long total = notificationEntityPage.getTotalElements();
        List<NotificationEntity> notifications = new ArrayList<>();
        for(NotificationEntity notification : tempNotifications){
            NotificationReadEntity readEntity = notificationReadRepository.findByNotificationNotificationId(notification.getNotificationId());
            if(readEntity == null)
                notifications.add(notification);
            else{
                if(readEntity.getIsDeleted() && readEntity.getUser().getUserId().equals(userId)) {
                    total--;
                    continue;
                }
                notifications.add(notification);
            }
        }

        List<NotificationDto> dtos = notifications.stream()
                .map(NotificationMapper.INSTANCE::toResponseNotificationDto)
                .toList();

        return new ResponseNotificationDto(dtos, total);
    }

    public ResponseUnreadNotificationDto getHasUnreadNotification(long userId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);

        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        List<NotificationEntity> notificationEntities = notificationRepository.findAllByToUserUserId(userId);

        Boolean hasUnread = false;

        for (NotificationEntity notification : notificationEntities) {
            NotificationReadEntity readEntity = notificationReadRepository.findByNotificationNotificationId(notification.getNotificationId());
            if (readEntity == null) {
                hasUnread = true;
                break;
            }
        }


        return new ResponseUnreadNotificationDto(hasUnread);
    }

    public ResponseNotificationDto getUnreadNotification(long userId, String type) {
        UserEntity user = userRepository.findByUserId(userId);

        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        List<NotificationEntity> notificationEntities = notificationRepository.findAllByToUserUserId(userId);

        List<Long> notificationIds = new ArrayList<>();

        for (NotificationEntity notification : notificationEntities) {
            NotificationReadEntity readEntity = notificationReadRepository.findByNotificationNotificationId(notification.getNotificationId());
            if (readEntity == null)
                notificationIds.add(notification.getNotificationId());
        }

        switch (type)
        {
            case "a" :
                notificationEntities = notificationRepository.findAllByNotificationIdInAndTypeIdIn(notificationIds,TYPE_ALL);
                break;
            case "n" :
                notificationEntities = notificationRepository.findAllByNotificationIdInAndTypeIdIn(notificationIds,TYPE_NOTICE);
                break;
            case "s" :
                notificationEntities = notificationRepository.findAllByNotificationIdInAndTypeIdIn(notificationIds,TYPE_SHARE);
                break;
            case "r" :
                notificationEntities = notificationRepository.findAllByNotificationIdInAndTypeIdIn(notificationIds,TYPE_RECORD);
                break;
            default:
                throw new CustomException(ErrorCode.NOFI_TYPE_BAD_REQUEST);
        }

        List<NotificationDto> dtos = notificationEntities.stream()
                .map(NotificationMapper.INSTANCE::toResponseNotificationDto)
                .toList();
        long total = notificationEntities.size();

        return new ResponseNotificationDto(dtos, total);
    }

    public ResponseUnreadCountDto getCountUnreadNotification(long userId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);

        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        List<NotificationEntity> notificationEntities = notificationRepository.findAllByToUserUserId(userId);
        List<Long> notificationIds = new ArrayList<>();

        for (NotificationEntity notification : notificationEntities) {
            NotificationReadEntity readEntity = notificationReadRepository.findByNotificationNotificationId(notification.getNotificationId());
            if (readEntity == null)
                notificationIds.add(notification.getNotificationId());
        }

        notificationEntities = notificationRepository.findAllByNotificationIdInAndTypeIdIn(notificationIds,TYPE_ALL);
        long allCount = notificationEntities.size();
        notificationEntities = notificationRepository.findAllByNotificationIdInAndTypeIdIn(notificationIds,TYPE_NOTICE);
        long noticeCount = notificationEntities.size();
        notificationEntities = notificationRepository.findAllByNotificationIdInAndTypeIdIn(notificationIds,TYPE_SHARE);
        long shareCount = notificationEntities.size();
        notificationEntities = notificationRepository.findAllByNotificationIdInAndTypeIdIn(notificationIds,TYPE_RECORD);
        long documentCount = notificationEntities.size();


        return new ResponseUnreadCountDto(allCount,noticeCount,shareCount,documentCount);
    }

    public void postNotificationRead(long userId, long notificationId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);

        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        NotificationReadEntity notificationRead = notificationReadRepository.findByNotificationNotificationId(notificationId);
        if(notificationRead != null ) throw new CustomException(ErrorCode.NOTIFICATION_READ_DUPLICATED);

        NotificationEntity notification = notificationRepository.findByNotificationId(notificationId);
        if(notification == null) throw new CustomException(ErrorCode.NOT_FOUND_NOTIFICATION);

        if(notification.getToUser().getUserId() != userId) throw new CustomException(ErrorCode.MISMATCH_NOTIFICATION_OWNER);



        NotificationReadEntity readEntity = new NotificationReadEntity();
        readEntity.setNotification(notification);
        readEntity.setUser(user);
        readEntity.setIsDeleted(false);
        notificationReadRepository.save(readEntity);

    }// 주인 에러, 충돌에러
    public void deleteNotification(long userId, long notificationId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);

        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        NotificationEntity notification = notificationRepository.findByNotificationId(notificationId);
        if(notification == null) throw new CustomException(ErrorCode.NOT_FOUND_NOTIFICATION);

        if(notification.getToUser().getUserId() != userId) throw new CustomException(ErrorCode.MISMATCH_NOTIFICATION_OWNER);

        if(notification.getTypeId() != '1'){
            notificationRepository.delete(notification);
        }
        else {
            NotificationReadEntity notificationRead = notificationReadRepository.findByNotificationNotificationId(notificationId);
            if(notificationRead != null){
                notificationRead.setIsDeleted(true);
                notificationReadRepository.save(notificationRead);
            }
            else{
                NotificationReadEntity readEntity = new NotificationReadEntity();
                readEntity.setNotification(notification);
                readEntity.setUser(user);
                readEntity.setIsDeleted(true);
                notificationReadRepository.save(readEntity);
            }
        }

    }// 주인 에러, 충돌에러

}
