package org.y2k2.globa.fixture.user.data;

import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;

public record AnalysisData(
        FolderEntity folder,
        FolderRoleEntity folderRole,
        RecordEntity record,
        QuizEntity quiz,
        QuizAttemptEntity quizAttempt,
        StudyEntity study,
        KeywordEntity keyword
) {
}
