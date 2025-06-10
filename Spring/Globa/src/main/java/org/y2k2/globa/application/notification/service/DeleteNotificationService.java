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
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.notificationread.entity.NotificationReadEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class DeleteNotificationService {
    private final FindUserUseCase findUserUseCase;
    private final VerifyModifyNotificationUseCase verifyModifyNotificationUseCase;

    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;

    public void delete(Long notificationId, Long userId) {
        NotificationEntity notification = notificationRepository.getNotification(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NOTIFICATION));

        verifyModifyNotificationUseCase.execute(VerifyModifyNotificationCommand.of(
                notification,
                userId
        ));

        UserEntity user = findUserUseCase.execute(userId);

        NotificationType type = notification.getType();
        if (type == NotificationType.NOTICE || isShareNotification(notification)) {
            // 공지, 공유 관련 알림은 불특정 다수에게 전달되는 알림으로 특정 사용자만 삭제한 것으로 처리
            NotificationReadEntity readEntity = notificationReadRepository.getNotificationRead(notificationId)
                    .orElseGet(() -> NotificationReadMapper.INSTANCE.toEntity(notification, user, true));

            notificationReadRepository.save(readEntity);
        } else {
            notificationRepository.delete(notification);
        }
    }

    private boolean isShareNotification(NotificationEntity notification) {
        return notification.getType() == NotificationType.SHARE_FOLDER_ADD_COMMENT ||
                notification.getType() == NotificationType.SHARE_FOLDER_ADD_FILE ||
                notification.getType() == NotificationType.SHARE_FOLDER_ADD_USER;
    }
}
