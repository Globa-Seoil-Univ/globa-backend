package org.y2k2.globa.infrastructure.persistence.study.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.study.repository.StudyRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;
import org.y2k2.globa.infrastructure.persistence.study.projection.StudyTimeProjection;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class StudyRepositoryImpl implements StudyRepository {
    private final StudyJpaRepository studyJpaRepository;

    @Override
    public StudyEntity save(StudyEntity study) {
        return studyJpaRepository.save(study);
    }

    @Override
    public List<StudyEntity> getAllStudies(Long userId, Long recordId) {
        return studyJpaRepository.findAllByUser_UserIdAndRecordRecordId(userId, recordId);
    }

    @Override
    public List<StudyTimeProjection> getStudyTimeInWeek(Long userId) {
        LocalDateTime intervalDay = new CustomTimestamp().getTimestamp().minusDays(7);
        return studyJpaRepository.findStudyTimeByInDays(userId, intervalDay);
    }

    @Override
    public Optional<StudyEntity> getStudy(UserEntity user, RecordEntity record) {
        return studyJpaRepository.findByCreatedTime(user, record);
    }
}
