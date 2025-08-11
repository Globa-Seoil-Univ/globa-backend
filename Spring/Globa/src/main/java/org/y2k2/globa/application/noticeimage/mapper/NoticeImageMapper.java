package org.y2k2.globa.application.noticeimage.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;
import org.y2k2.globa.infrastructure.persistence.noticeimage.entity.NoticeImageEntity;
import org.y2k2.globa.application.common.mapper.CustomTimestampMapper;

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
