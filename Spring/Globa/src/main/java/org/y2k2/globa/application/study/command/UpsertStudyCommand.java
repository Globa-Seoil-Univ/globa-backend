package org.y2k2.globa.application.study.command;

import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

public record UpsertStudyCommand(
        UserEntity user,
        RecordEntity record,
        Integer studyTime
) {
    public static UpsertStudyCommand of(UserEntity user, RecordEntity record, Integer studyTime) {
        return new UpsertStudyCommand(user, record, studyTime);
    }
}
