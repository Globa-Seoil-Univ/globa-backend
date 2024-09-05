package org.y2k2.globa.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.Projection.NotificationProjection;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.FolderRoleEntity;
import org.y2k2.globa.entity.NotificationEntity;

import java.util.Collection;
import java.util.Map;

@Mapper(uses = {CustomTimestampMapper.class})
public interface NotificationMapper {
    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    @Mapping(source = "fromUser", target = "fromUser")
    @Mapping(source = "notice", target = "notice")
    NotificationEntity toNotificationWithNotice(RequestNotificationWithNoticeDto dto);

    @Mapping(source = "fromUser", target = "fromUser")
    @Mapping(source = "toUser", target = "toUser")
    @Mapping(source = "inquiry", target = "inquiry")
    NotificationEntity toNotificationWithInquiry(RequestNotificationWithInquiryDto dto);

    @Mapping(source = "fromUser", target = "fromUser")
    @Mapping(source = "toUser", target = "toUser")
    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "folderShare", target = "folderShare")
    NotificationEntity toNotificationWithInvitation(RequestNotificationWithInvitationDto dto);

    @Mapping(source = "fromUser", target = "fromUser")
    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "folderShare", target = "folderShare")
    NotificationEntity toNotificationWithFolderShareAddUser(RequestNotificationWithFolderShareAddUserDto dto);

    @Mapping(source = "fromUser", target = "fromUser")
    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "folderShare", target = "folderShare")
    @Mapping(source = "record", target = "record")
    @Mapping(source = "comment", target = "comment")
    NotificationEntity toNotificationWithFolderShareComment(RequestNotificationWithFolderShareCommentDto dto);

    @Mapping(source = "entity.notificationId", target = "notificationId")
    @Mapping(source = "entity.typeId", target = "type")
    @Mapping(source = "entity.createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    @Mapping(source = "entity.notice", target = "notice")
    @Mapping(source = "entity.fromUser", target = "user")
    @Mapping(source = "entity.folderShare", target = "share")
    @Mapping(source = "entity.folder", target = "folder")
    @Mapping(source = "entity.record", target = "record")
    @Mapping(source = "entity.comment", target = "comment")
    @Mapping(source = "entity.inquiry", target = "inquiry")
    NotificationDto toResponseNotificationDto(NotificationEntity entity);

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

    default Boolean mapIsRead(Integer isRead) {
        return isRead != null && isRead == 1;
    }

    @AfterMapping
    default NotificationDto deleteEmpty(@MappingTarget NotificationDto entity) {
        // 빈 객체를 null로 설정
        if (entity.getNotice().getNoticeId() == null && entity.getNotice().getTitle() == null) {
            entity.setNotice(null);
        }
        if (entity.getUser().getProfile() == null && entity.getUser().getName() == null) {
            entity.setUser(null);
        }
        if (entity.getShare().getShareId() == null) {
            entity.setShare(null);
        }
        if (entity.getFolder().getFolderId() == null && entity.getFolder().getTitle() == null) {
            entity.setFolder(null);
        }
        if (entity.getRecord().getRecordId() == null && entity.getRecord().getTitle() == null) {
            entity.setRecord(null);
        }
        if (entity.getComment().getCommentId() == null && entity.getComment().getContent() == null) {
            entity.setComment(null);
        }
        if (entity.getInquiry().getInquiryId() == null && entity.getInquiry().getTitle() == null) {
            entity.setInquiry(null);
        }

        return entity;
    }
}
