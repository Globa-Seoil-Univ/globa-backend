package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.NotificationEntity;

@Mapper
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
}
