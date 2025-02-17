package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.response.analysis.ResponseRecordAnalysisDto;
import org.y2k2.globa.dto.response.section.ResponseSectionDto;
import org.y2k2.globa.dto.response.summary.ResponseDetailSummaryDto;
import org.y2k2.globa.entity.SectionEntity;
import org.y2k2.globa.entity.SummaryEntity;

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
