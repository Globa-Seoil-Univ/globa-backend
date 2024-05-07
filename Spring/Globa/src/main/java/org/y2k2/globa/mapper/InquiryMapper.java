package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.InquiryDto;
import org.y2k2.globa.dto.ResponseInquiryDetailDto;
import org.y2k2.globa.dto.ResponseInquiryDto;
import org.y2k2.globa.entity.AnswerEntity;
import org.y2k2.globa.entity.InquiryEntity;

@Mapper
public interface InquiryMapper {
    InquiryMapper INSTANCE = Mappers.getMapper(InquiryMapper.class);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "solved", target = "solved")
    InquiryDto toInquiryDto(InquiryEntity inquiry);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "content")
    ResponseInquiryDetailDto toResponseInquiryDetailDto(InquiryEntity inquiry);

    @Mapping(source = "inquiry.title", target = "title")
    @Mapping(source = "inquiry.content", target = "content")
    @Mapping(source = "answer.title", target = "answer.title")
    @Mapping(source = "answer.content", target = "answer.content")
    ResponseInquiryDetailDto toResponseInquiryDetailDto(InquiryEntity inquiry, AnswerEntity answer);
}
