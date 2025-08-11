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

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateNotificationUseCase implements VoidUseCase<CreateNotificationCommand> {
    private final NotificationRepository notificationRepository;

    @Override
    public void execute(CreateNotificationCommand command) {
        NotificationEntity notification = createNotification(command.sendMessage());
        notificationRepository.save(notification);
    }

    private NotificationEntity createNotification(SendMessage sendMessage) {
        NotificationEntity notification;

        switch (sendMessage.getNotificationType()) {
            case NOTICE:
                notification = NotificationMapper.INSTANCE.toNotificationWithBasic((RequestNotificationWithTopicDto) sendMessage);
                break;
            case UPLOAD_SUCCESS:
                notification = NotificationMapper.INSTANCE.toNotificationWithUploadSuccess((RequestNotificationWithUploadSuccessDto) sendMessage);
                break;
            case UPLOAD_FAILED:
                notification = NotificationMapper.INSTANCE.toNotificationWithUploadFailed(sendMessage);
                break;
            case SHARE_FOLDER_ADD_FILE, SHARE_FOLDER_ADD_USER:
                notification = NotificationMapper.INSTANCE.toNotificationWithFolderShare((RequestNotificationWithFolderShareDto) sendMessage);
                break;
            case SHARE_FOLDER_ADD_COMMENT:
                notification = NotificationMapper.INSTANCE.toNotificationWithFolderShareComment((RequestNotificationWithFolderShareCommentDto) sendMessage);
                break;
            case INQUIRY:
                notification = NotificationMapper.INSTANCE.toNotificationWithInquiry((RequestNotificationWithInquiryDto) sendMessage);
                break;
            case SHARE_FOLDER_INVITE:
                notification = NotificationMapper.INSTANCE.toNotificationWithInvitation((RequestNotificationWithInvitationDto) sendMessage);
                break;
            default:
                return null;
        }

        return notification;
    }
}
