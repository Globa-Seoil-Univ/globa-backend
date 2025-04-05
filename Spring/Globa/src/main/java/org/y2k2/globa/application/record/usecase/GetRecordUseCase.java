package org.y2k2.globa.application.record.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.analysis.dto.response.ResponseRecordAnalysisDto;
import org.y2k2.globa.application.analysis.mapper.AnalysisMapper;
import org.y2k2.globa.application.folder.dto.response.ResponseDetailFolderDto;
import org.y2k2.globa.application.folder.mapper.FolderMapper;
import org.y2k2.globa.application.hightlight.dto.response.ResponseDetailHighlightDto;
import org.y2k2.globa.application.hightlight.mapper.HighlightMapper;
import org.y2k2.globa.application.record.command.GetRecordCommand;
import org.y2k2.globa.application.record.dto.response.ResponseRecordDetailDto;
import org.y2k2.globa.application.record.mapper.RecordMapper;
import org.y2k2.globa.application.section.dto.response.ResponseSectionDto;
import org.y2k2.globa.application.section.mapper.SectionMapper;
import org.y2k2.globa.application.summary.dto.response.ResponseDetailSummaryDto;
import org.y2k2.globa.application.summary.mapper.SummaryMapper;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetRecordUseCase implements UseCase<GetRecordCommand, List<ResponseSectionDto>> {
    @Override
    public List<ResponseSectionDto> execute(GetRecordCommand command) {
        List<ResponseSectionDto> responseSections = new ArrayList<>();

        for (SectionEntity section : command.sections()) {
            AnalysisEntity analysisToSection = command.analyses().stream()
                    .filter(analysis -> analysis.getSection().getSectionId().equals(section.getSectionId()))
                    .findFirst()
                    .orElse(null);
            List<HighlightEntity> highlightToSection = command.highlights().stream()
                    .filter(highlight -> highlight.getSection().getSectionId().equals(section.getSectionId()))
                    .toList();
            List<SummaryEntity> summaryToSection = command.summaries().stream()
                    .filter(summary -> summary.getSection().getSectionId().equals(section.getSectionId()))
                    .toList();

            List<ResponseDetailHighlightDto> responseHighlights = highlightToSection.stream()
                    .map(HighlightMapper.INSTANCE::toResponseDetailHighlightDto)
                    .toList();
            ResponseRecordAnalysisDto responseAnalysis = AnalysisMapper.INSTANCE.toResponseRecordAnalysisDto(analysisToSection, responseHighlights);
            List<ResponseDetailSummaryDto> responseSummaries = summaryToSection.stream()
                    .map(SummaryMapper.INSTANCE::toResponseDetailSummaryDto)
                    .toList();

            responseSections.add(SectionMapper.INSTANCE.toResponseDetailSummaryDto(section, responseAnalysis, responseSummaries));
        }

        return responseSections;
    }
}
