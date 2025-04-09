package org.y2k2.globa.application.notification.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.*;
import org.y2k2.globa.application.notification.mapper.NotificationMapper;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateNotificationUseCase implements VoidUseCase<CreateNotificationCommand> {
    private final NotificationRepository notificationRepository;

    @Override
    public void execute(CreateNotificationCommand command) {
        log.info("Save Notifications count = {}", command.sendMessages()
                .size());
        List<NotificationEntity> notifications = new ArrayList<>();

        for (SendMessage sendMessage : command.sendMessages()) {
            NotificationEntity notification = createNotification(sendMessage);

            if (notification == null) {
                log.warn("Notification is null. userId = {}, name = {}", sendMessage.getReceiver().getUserId(), sendMessage.getReceiver().getName());
                continue;
            }

            notifications.add(notification);
        }

        notificationRepository.saveAll(notifications);
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
}
