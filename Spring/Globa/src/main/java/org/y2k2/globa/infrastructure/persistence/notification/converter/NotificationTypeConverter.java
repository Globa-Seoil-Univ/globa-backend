package org.y2k2.globa.infrastructure.persistence.notification.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;

@Slf4j
@Converter(autoApply = true)
public class NotificationTypeConverter implements AttributeConverter<NotificationType, Character> {
    @Override
    public Character convertToDatabaseColumn(NotificationType notificationType) {
        if (notificationType == null) {
            log.error("NotificationType is null");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return notificationType.getTypeId();
    }

    @Override
    public NotificationType convertToEntityAttribute(Character character) {
        if (character == null) {
            return null;
        }

        if (character == NotificationType.NOTICE.getTypeId()) {
            return NotificationType.NOTICE;
        } else if (character == NotificationType.SHARE_FOLDER_INVITE.getTypeId()) {
            return NotificationType.SHARE_FOLDER_INVITE;
        } else if (character == NotificationType.SHARE_FOLDER_ADD_FILE.getTypeId()) {
            return NotificationType.SHARE_FOLDER_ADD_FILE;
        } else if (character == NotificationType.SHARE_FOLDER_ADD_USER.getTypeId()) {
            return NotificationType.SHARE_FOLDER_ADD_USER;
        } else if (character == NotificationType.SHARE_FOLDER_ADD_COMMENT.getTypeId()) {
            return NotificationType.SHARE_FOLDER_ADD_COMMENT;
        } else if (character == NotificationType.UPLOAD_SUCCESS.getTypeId()) {
            return NotificationType.UPLOAD_SUCCESS;
        } else if (character == NotificationType.UPLOAD_FAILED.getTypeId()) {
            return NotificationType.UPLOAD_FAILED;
        } else if (character == NotificationType.INQUIRY.getTypeId()) {
            return NotificationType.INQUIRY;
        } else {
            log.error("Unknown NotificationType code = {}", character);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
