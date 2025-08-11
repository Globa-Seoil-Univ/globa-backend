package org.y2k2.globa.domain.study.repository;

import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.study.projection.StudyTimeProjection;

import java.util.List;
import java.util.Optional;

public interface StudyRepository {
    StudyEntity save(StudyEntity study);

    List<StudyEntity> getAllStudies(Long userId, Long recordId);
    List<StudyTimeProjection> getStudyTimeInWeek(Long userId);

    Optional<StudyEntity> getStudy(UserEntity user, RecordEntity record);
}
