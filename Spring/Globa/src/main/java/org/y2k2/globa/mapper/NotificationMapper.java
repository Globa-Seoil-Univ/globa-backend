package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.NotificationEntity;

@Mapper(uses = {CustomTimestampMapper.class})
public interface NotificationMapper {
    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    @Mapping(source = "toUser", target = "toUser")
    @Mapping(source = "notice", target = "notice")
    NotificationEntity toNotificationWithNotice(RequestNotificationWithNoticeDto dto);

    @Mapping(source = "toUser", target = "toUser")
    @Mapping(source = "fromUser", target = "fromUser")
    @Mapping(source = "inquiry", target = "inquiry")
    NotificationEntity toNotificationWithInquiry(RequestNotificationWithInquiryDto dto);

    @Mapping(source = "toUser", target = "toUser")
    @Mapping(source = "fromUser", target = "fromUser")
    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "folderShare", target = "folderShare")
    NotificationEntity toNotificationWithInvitation(RequestNotificationWithInvitationDto dto);

    @Mapping(source = "toUser", target = "toUser")
    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "folderShare", target = "folderShare")
    NotificationEntity toNotificationWithFolderShareAddUser(RequestNotificationWithFolderShareAddUserDto dto);

    @Mapping(source = "toUser", target = "toUser")
    @Mapping(source = "folder", target = "folder")
    @Mapping(source = "folderShare", target = "folderShare")
    @Mapping(source = "record", target = "record")
    @Mapping(source = "comment", target = "comment")
    NotificationEntity toNotificationWithFolderShareComment(RequestNotificationWithFolderShareCommentDto dto);

    @Mapping(source = "entity.notificationId", target = "notificationId")
    @Mapping(source = "entity.typeId", target = "type")
    @Mapping(source = "entity.createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    @Mapping(source = "entity.notice", target = "notice")
    @Mapping(source = "entity.toUser", target = "user")
    @Mapping(source = "entity.folderShare", target = "share")
    @Mapping(source = "entity.folder", target = "folder")
    @Mapping(source = "entity.record", target = "record")
    @Mapping(source = "entity.comment", target = "comment")
    @Mapping(source = "entity.inquiry", target = "inquiry")
    NotificationDto toResponseNotificationDto(NotificationEntity entity);
}
