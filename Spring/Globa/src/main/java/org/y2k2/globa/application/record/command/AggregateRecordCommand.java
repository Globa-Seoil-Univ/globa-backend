package org.y2k2.globa.application.record.command;

import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

import java.util.List;

public record AggregateRecordCommand(
        Long recordId,
        List<SectionEntity> sections,
        List<AnalysisEntity> analyses,
        List<HighlightEntity> highlights,
        List<SummaryEntity> summaries
) {
    public static AggregateRecordCommand of(
            Long recordId,
            List<SectionEntity> sections,
            List<AnalysisEntity> analyses,
            List<HighlightEntity> highlights,
            List<SummaryEntity> summaries
    ) {
        return new AggregateRecordCommand(recordId, sections, analyses, highlights, summaries);
    }
}
