package org.y2k2.globa.application.notification.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.notification.command.VerifyModifyNotificationCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;

@Component
@RequiredArgsConstructor
public class VerifyModifyNotificationUseCase implements VoidUseCase<VerifyModifyNotificationCommand> {
    private final FolderShareRepository folderShareRepository;

    private boolean isAccessibleNotification(NotificationEntity notification, Long userId) {
        return notification.getReceiver().getUserId().equals(userId);
    }

    private boolean isShareNotification(NotificationEntity notification) {
        return notification.getTypeId() == NotificationType.SHARE_FOLDER_ADD_COMMENT ||
                notification.getTypeId() == NotificationType.SHARE_FOLDER_ADD_FILE ||
                notification.getTypeId() == NotificationType.SHARE_FOLDER_ADD_USER;
    }

    private boolean isAccessibleShareNotification(NotificationEntity notification, Long userId) {
        if (notification.getFolder() == null || notification.getFolder().getFolderId() == null) {
            return false;
        }

        return folderShareRepository.isAccessible(userId, notification.getFolder().getFolderId());
    }

    @Override
    public void execute(VerifyModifyNotificationCommand command) {
        if (!isAccessibleNotification(command.notification(), command.userId())) {
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION);
        }

        if (isShareNotification(command.notification()) && !isAccessibleShareNotification(command.notification(), command.userId())) {
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION);
        }
    }
}
