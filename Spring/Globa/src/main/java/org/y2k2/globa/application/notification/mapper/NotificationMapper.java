package org.y2k2.globa.application.notification.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.common.mapper.CustomTimestampMapper;
import org.y2k2.globa.application.common.mapper.CustomTimestampTranslator;
import org.y2k2.globa.application.common.mapper.MapCreatedTime;
import org.y2k2.globa.application.notification.dto.common.*;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationProjection;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;

@Mapper(uses = CustomTimestampMapper.class)
public interface NotificationMapper {
    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    @Mapping(source = "sender", target = "sender")
    @Mapping(source = "notice", target = "notice")
    @Mapping(source = "notificationType", target = "type")
    NotificationEntity toNotificationWithBasic(RequestNotificationWithTopicDto dto);

    @Mapping(source = "sender", target = "sender")
    @Mapping(source = "receiver", target = "receiver")
    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "record", target = "record")
    @Mapping(source = "notificationType", target = "type")
    NotificationEntity toNotificationWithUploadSuccess(RequestNotificationWithUploadSuccessDto dto);

    @Mapping(source = "sender", target = "sender")
    @Mapping(source = "receiver", target = "receiver")
    @Mapping(source = "notificationType", target = "type")
    NotificationEntity toNotificationWithUploadFailed(SendMessage dto);

    @Mapping(source = "sender", target = "sender")
    @Mapping(source = "receiver", target = "receiver")
    @Mapping(source = "inquiry", target = "inquiry")
    @Mapping(source = "notificationType", target = "type")
    NotificationEntity toNotificationWithInquiry(RequestNotificationWithInquiryDto dto);

    @Mapping(source = "sender", target = "sender")
    @Mapping(source = "receiver", target = "receiver")
    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "folderShare", target = "folderShare")
    @Mapping(source = "notificationType", target = "type")
    NotificationEntity toNotificationWithInvitation(RequestNotificationWithInvitationDto dto);

    @Mapping(source = "sender", target = "sender")
    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "folderShare", target = "folderShare")
    @Mapping(source = "notificationType", target = "type")
    NotificationEntity toNotificationWithFolderShare(RequestNotificationWithFolderShareDto dto);

    @Mapping(source = "sender", target = "sender")
    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "folderShare", target = "folderShare")
    @Mapping(source = "record", target = "record")
    @Mapping(source = "comment", target = "comment")
    @Mapping(source = "notificationType", target = "type")
    NotificationEntity toNotificationWithFolderShareComment(RequestNotificationWithFolderShareCommentDto dto);

    @Mapping(source = "notificationId", target = "notificationId")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "isRead", target = "isRead")
    @Mapping(source = "noticeId", target = "notice.noticeId")
    @Mapping(source = "noticeThumbnail", target = "notice.thumbnail")
    @Mapping(source = "noticeTitle", target = "notice.title")
    @Mapping(source = "noticeContent", target = "notice.content")
    @Mapping(source = "userProfile", target = "user.profile")
    @Mapping(source = "userName", target = "user.name")
    @Mapping(source = "shareId", target = "share.shareId")
    @Mapping(source = "folderId", target = "folder.folderId")
    @Mapping(source = "folderTitle", target = "folder.title")
    @Mapping(source = "recordId", target = "record.recordId")
    @Mapping(source = "recordTitle", target = "record.title")
    @Mapping(source = "commentId", target = "comment.commentId")
    @Mapping(source = "commentContent", target = "comment.content")
    @Mapping(source = "inquiryId", target = "inquiry.inquiryId")
    @Mapping(source = "inquiryTitle", target = "inquiry.title")
    @Mapping(source = "createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    NotificationDto toResponseNotificationDto(NotificationProjection entity);

    @AfterMapping
    default NotificationDto deleteEmpty(@MappingTarget NotificationDto entity) {
        // 빈 객체를 null로 설정
        if (
                entity.getNotice() == null
                        || entity.getNotice().getNoticeId() == null
                        || entity.getNotice().getTitle() == null
        ) {
            entity.setNotice(null);
        }
        if (
                entity.getUser() == null
                        || entity.getUser().getProfile() == null
                        || entity.getUser().getName() == null
        ) {
            entity.setUser(null);
        }
        if (entity.getShare() == null || entity.getShare().getShareId() == null) {
            entity.setShare(null);
        }
        if (
                entity.getFolder() == null
                        || entity.getFolder().getFolderId() == null
                        || entity.getFolder().getTitle() == null
        ) {
            entity.setFolder(null);
        }
        if (
                entity.getRecord() == null
                        || entity.getRecord().getRecordId() == null
                        || entity.getRecord().getTitle() == null
        ) {
            entity.setRecord(null);
        }
        if (
                entity.getComment() == null
                        || entity.getComment().getCommentId() == null
                        || entity.getComment().getContent() == null
        ) {
            entity.setComment(null);
        }
        if (
                entity.getInquiry() == null
                        || entity.getInquiry().getInquiryId() == null
                        || entity.getInquiry().getTitle() == null
        ) {
            entity.setInquiry(null);
        }

        if (entity.getType() != null) {
            entity.setType(String.valueOf(NotificationType.fromValue(entity.getType())));
        }

        return entity;
    }
}
