package org.y2k2.globa.application.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.notification.command.VerifyModifyNotificationCommand;
import org.y2k2.globa.application.notification.usecase.VerifyModifyNotificationUseCase;
import org.y2k2.globa.application.notificationread.mapper.NotificationReadMapper;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.domain.notificationread.repository.NotificationReadRepository;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notificationread.entity.NotificationReadEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReadNotificationService {
    private final FindUserUseCase findUserUseCase;
    private final VerifyModifyNotificationUseCase verifyModifyNotificationUseCase;

    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;

    public void read(Long notificationId, Long userId) {
        Optional<NotificationReadEntity> notificationRead = notificationReadRepository.getNotificationRead(notificationId);
        if (notificationRead.isPresent()) {
            return;
        }

        NotificationEntity notification = notificationRepository.getNotification(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NOTIFICATION));

        verifyModifyNotificationUseCase.execute(VerifyModifyNotificationCommand.of(
                notification,
                userId
        ));

        UserEntity user = findUserUseCase.execute(userId);
        NotificationReadEntity readEntity = NotificationReadMapper.INSTANCE.toEntity(notification, user, false);
        notificationReadRepository.save(readEntity);
    }
}
