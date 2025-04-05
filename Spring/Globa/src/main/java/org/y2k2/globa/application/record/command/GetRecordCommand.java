package org.y2k2.globa.application.record.command;

import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

import java.util.List;

public record GetRecordCommand(
        List<SectionEntity> sections,
        List<AnalysisEntity> analyses,
        List<HighlightEntity> highlights,
        List<SummaryEntity> summaries
) {
    public static GetRecordCommand of(
            List<SectionEntity> sections,
            List<AnalysisEntity> analyses,
            List<HighlightEntity> highlights,
            List<SummaryEntity> summaries
    ) {
        return new GetRecordCommand(sections, analyses, highlights, summaries);
    }
}
