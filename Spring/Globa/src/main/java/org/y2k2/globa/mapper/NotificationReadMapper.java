package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.entity.NotificationEntity;
import org.y2k2.globa.entity.NotificationReadEntity;
import org.y2k2.globa.entity.UserEntity;

@Mapper(uses = {CustomTimestampMapper.class})
public interface NotificationReadMapper {
    NotificationReadMapper INSTANCE = Mappers.getMapper(NotificationReadMapper.class);

    @Mapping(source = "notification", target = "notification")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "isDeleted", target = "isDeleted")
    @Mapping(target = "createdTime", ignore = true)
    NotificationReadEntity toEntity(NotificationEntity notification, UserEntity user, boolean isDeleted);
}
