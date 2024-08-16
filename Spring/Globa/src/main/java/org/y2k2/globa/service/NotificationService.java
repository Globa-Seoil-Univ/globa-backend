package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.dto.NotificationDto;
import org.y2k2.globa.dto.ResponseNotificationDto;
import org.y2k2.globa.dto.ResponseUnreadNotificationDto;
import org.y2k2.globa.entity.NotificationEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.mapper.NotificationMapper;
import org.y2k2.globa.repository.NotificationRepository;
import org.y2k2.globa.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public ResponseNotificationDto getNotifications(long userId, int count, int page) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);

        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<NotificationEntity> notificationEntityPage = notificationRepository.findAllByToUserOrTypeIdInOrderByCreatedTimeDesc(pageable, user, new char[]{'1'});
        List<NotificationEntity> notifications = notificationEntityPage.getContent();
        List<NotificationDto> dtos = notifications.stream()
                .map(NotificationMapper.INSTANCE::toResponseNotificationDto)
                .toList();
        long total = notificationEntityPage.getTotalElements();

        return new ResponseNotificationDto(dtos, total);
    }

    public ResponseUnreadNotificationDto getUnreadNotification(long userId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);

        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        Long hasUnread = notificationRepository.countByUserUserIdAndIsRead(userId, false);

        return new ResponseUnreadNotificationDto(hasUnread > 0);
    }



}
