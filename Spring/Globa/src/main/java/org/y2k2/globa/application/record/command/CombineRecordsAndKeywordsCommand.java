package org.y2k2.globa.application.record.command;

import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.List;

public record CombineRecordsAndKeywordsCommand(
        List<RecordEntity> records,
        Long total
) {
    public static CombineRecordsAndKeywordsCommand of(List<RecordEntity> records, Long total) {
        return new CombineRecordsAndKeywordsCommand(records, total);
    }
}
