package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import org.y2k2.globa.dto.RequestNoticeAddDto;
import org.y2k2.globa.dto.ResponseNoticeDetailDto;
import org.y2k2.globa.dto.ResponseNoticeIntroDto;
import org.y2k2.globa.entity.NoticeEntity;

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

    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "bgColor", target = "bgColor")
    NoticeEntity toEntity(RequestNoticeAddDto dto);
}
