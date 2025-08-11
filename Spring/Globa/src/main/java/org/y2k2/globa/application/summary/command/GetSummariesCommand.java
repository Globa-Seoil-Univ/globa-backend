package org.y2k2.globa.application.summary.command;

import java.util.List;

public record GetSummariesCommand(
        List<Long> sectionIds
) {
    public static GetSummariesCommand of(List<Long> sectionIds) {
        return new GetSummariesCommand(sectionIds);
    }
}
