package org.y2k2.globa.application.section.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.analysis.dto.response.ResponseRecordAnalysisDto;
import org.y2k2.globa.application.section.dto.response.ResponseSectionDto;
import org.y2k2.globa.application.summary.dto.response.ResponseDetailSummaryDto;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.application.comment.mapper.CustomTimestampMapper;
import org.y2k2.globa.mapper.CustomTimestampTranslator;
import org.y2k2.globa.mapper.MapCreatedTime;

import java.util.List;

@Mapper(uses = {CustomTimestampMapper.class})
public interface SectionMapper {
    SectionMapper INSTANCE = Mappers.getMapper(SectionMapper.class);

    @Mapping(source = "section.sectionId", target = "sectionId")
    @Mapping(source = "section.title", target = "title")
    @Mapping(source = "section.startTime", target = "startTime")
    @Mapping(source = "section.endTime", target = "endTime")
    @Mapping(source = "analyses", target = "analyses")
    @Mapping(source = "summaries", target = "summaries")
    @Mapping(source = "section.createdTime", target = "createdTime", qualifiedBy = {CustomTimestampTranslator.class, MapCreatedTime.class})
    ResponseSectionDto toResponseDetailSummaryDto(
            SectionEntity section,
            ResponseRecordAnalysisDto analyses,
            List<ResponseDetailSummaryDto> summaries
    );
}
