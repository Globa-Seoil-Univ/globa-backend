package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.common.answer.RequestAnswerDto;
import org.y2k2.globa.entity.AnswerEntity;
import org.y2k2.globa.entity.InquiryEntity;
import org.y2k2.globa.entity.UserEntity;

@Mapper
public interface AnswerMapper {
    AnswerMapper INSTANCE = Mappers.getMapper(AnswerMapper.class);

    @Mapping(source = "user", target = "user")
    @Mapping(source = "inquiry", target = "inquiry")
    @Mapping(source = "dto.title", target = "title")
    @Mapping(source = "dto.content", target = "content")
    @Mapping(target = "createdTime", ignore = true)
    AnswerEntity toEntity(UserEntity user, InquiryEntity inquiry, RequestAnswerDto dto);
}
