package org.y2k2.globa.application.analysis.command;

import java.util.List;

public record GetAnalysisCommand(
        List<Long> sectionIds
) {
    public static GetAnalysisCommand of(List<Long> sectionIds) {
        return new GetAnalysisCommand(sectionIds);
    }
}
