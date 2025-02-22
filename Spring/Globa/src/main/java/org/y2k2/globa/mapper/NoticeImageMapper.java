package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.request.notice.RequestNoticeAddDto;
import org.y2k2.globa.dto.response.notice.ResponseNoticeDetailDto;
import org.y2k2.globa.dto.response.notice.ResponseNoticeIntroDto;
import org.y2k2.globa.entity.DummyImageEntity;
import org.y2k2.globa.entity.NoticeEntity;
import org.y2k2.globa.entity.NoticeImageEntity;
import org.y2k2.globa.entity.UserEntity;

@Mapper(uses = CustomTimestampMapper.class)
public interface NoticeImageMapper {
    NoticeImageMapper INSTANCE = Mappers.getMapper(NoticeImageMapper.class);

    @Mapping(source = "notice", target = "notice")
    @Mapping(source = "entity.imagePath", target = "imagePath")
    @Mapping(source = "entity.imageSize", target = "imageSize")
    @Mapping(source = "entity.imageType", target = "imageType")
    @Mapping(target = "createdTime", ignore = true)
    NoticeImageEntity toEntity(NoticeEntity notice, DummyImageEntity entity);
}
