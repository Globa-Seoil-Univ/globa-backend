package org.y2k2.globa.application.record.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.analysis.dto.response.ResponseRecordAnalysisDto;
import org.y2k2.globa.application.analysis.mapper.AnalysisMapper;
import org.y2k2.globa.application.hightlight.dto.response.ResponseDetailHighlightDto;
import org.y2k2.globa.application.hightlight.mapper.HighlightMapper;
import org.y2k2.globa.application.record.command.AggregateRecordCommand;
import org.y2k2.globa.application.section.dto.response.ResponseSectionDto;
import org.y2k2.globa.application.section.mapper.SectionMapper;
import org.y2k2.globa.application.summary.dto.response.ResponseDetailSummaryDto;
import org.y2k2.globa.application.summary.mapper.SummaryMapper;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AggregateRecordUseCase implements UseCase<AggregateRecordCommand, List<ResponseSectionDto>> {
    @Cacheable(value = "aggregateRecord", key = "#command.recordId()")
    @Override
    public List<ResponseSectionDto> execute(AggregateRecordCommand command) {
        List<ResponseSectionDto> responseSections = new ArrayList<>();

        for (SectionEntity section : command.sections()) {
            // Record : Section => 1 : N
            // Section : Analysis => 1 : 1
            // 다수의 Section ID(최적화 In절 사용)를 통해 여러 개의 Analysis를 가져왔기 떄문에 findFirst 사용
            AnalysisEntity analysisToSection = command.analyses().stream()
                    .filter(analysis -> analysis.getSection().getSectionId().equals(section.getSectionId()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ANALYSIS));
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
