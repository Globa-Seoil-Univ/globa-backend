package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.InquiryDto;
import org.y2k2.globa.dto.ResponseInquiryDetailDto;
import org.y2k2.globa.entity.AnswerEntity;
import org.y2k2.globa.entity.InquiryEntity;

@Mapper(uses = CustomTimestampMapper.class)
public interface InquiryMapper {
    InquiryMapper INSTANCE = Mappers.getMapper(InquiryMapper.class);

    @Mapping(source = "inquiryId", target = "inquiryId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "solved", target = "solved")
    @Mapping(source = "createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    InquiryDto toInquiryDto(InquiryEntity inquiry);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    ResponseInquiryDetailDto toResponseInquiryDetailDto(InquiryEntity inquiry);

    @Mapping(source = "inquiry.title", target = "title")
    @Mapping(source = "inquiry.content", target = "content")
    @Mapping(source = "inquiry.createdTime", target = "createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    @Mapping(source = "answer.title", target = "answer.title")
    @Mapping(source = "answer.content", target = "answer.content")
    @Mapping(source = "answer.createdTime", target = "answer.createdTime", qualifiedBy = { CustomTimestampTranslator.class, MapCreatedTime.class })
    ResponseInquiryDetailDto toResponseInquiryDetailDto(InquiryEntity inquiry, AnswerEntity answer);
}
