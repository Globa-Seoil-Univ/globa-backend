package org.y2k2.globa.application.notice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.notice.dto.request.RequestNoticeAddDto;
import org.y2k2.globa.application.notice.dto.response.ResponseNoticeDetailDto;
import org.y2k2.globa.application.notice.dto.response.ResponseNoticeIntroDto;
import org.y2k2.globa.entity.NoticeEntity;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.application.comment.mapper.CustomTimestampMapper;
import org.y2k2.globa.mapper.CustomTimestampTranslator;
import org.y2k2.globa.mapper.MapCreatedTime;

@Mapper(uses = CustomTimestampMapper.class)
public interface NoticeMapper {
    NoticeMapper INSTANCE = Mappers.getMapper(NoticeMapper.class);

    @Mapping(source = "noticeId", target = "noticeId")
    @Mapping(source = "thumbnailPath", target = "thumbnail")
    @Mapping(source = "bgColor", target = "bgColor")
    ResponseNoticeIntroDto toIntroResponseDto(NoticeEntity noticeEntity);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    ResponseNoticeDetailDto toDetailResponseDto(NoticeEntity noticeEntity);

    @Mapping(source = "dto.title", target = "title")
    @Mapping(source = "dto.content", target = "content")
    @Mapping(source = "dto.bgColor", target = "bgColor")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "file.storePath", target = "thumbnailPath")
    @Mapping(source = "file.extension", target = "thumbnailType")
    @Mapping(source = "file.size", target = "thumbnailSize")
    @Mapping(target = "createdTime", ignore = true)
    NoticeEntity toEntity(RequestNoticeAddDto dto, UserEntity user, FileDto file);
}
