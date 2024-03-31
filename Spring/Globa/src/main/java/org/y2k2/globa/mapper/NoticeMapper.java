package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import org.y2k2.globa.dto.NoticeAddRequestDto;
import org.y2k2.globa.dto.NoticeDetailResponseDto;
import org.y2k2.globa.dto.NoticeIntroResponseDto;
import org.y2k2.globa.entity.NoticeEntity;

@Mapper(uses = CustomTimestampMapper.class)
public interface NoticeMapper {
    NoticeMapper INSTANCE = Mappers.getMapper(NoticeMapper.class);

    @Mapping(source = "noticeId", target = "noticeId")
    @Mapping(source = "thumbnailPath", target = "thumbnail")
    @Mapping(source = "bgColor", target = "bgColor")
    NoticeIntroResponseDto toIntroResponseDto(NoticeEntity noticeEntity);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    NoticeDetailResponseDto toDetailResponseDto(NoticeEntity noticeEntity);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "bgColor", target = "bgColor")
    NoticeEntity toEntity(NoticeAddRequestDto dto);
}
