package org.y2k2.globa.infrastructure.persistence.study.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.study.repository.StudyRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;
import org.y2k2.globa.infrastructure.persistence.study.projection.StudyTimeProjection;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class StudyRepositoryImpl implements StudyRepository {
    private final StudyJpaRepository studyJpaRepository;

    @Override
    public void save(StudyEntity entity) {
        studyJpaRepository.save(entity);
    }

    @Override
    public List<StudyEntity> getAllStudies(UserEntity user, Long recordId) {
        return studyJpaRepository.findAllByUserAndRecordRecordId(user, recordId);
    }

    @Override
    public List<StudyTimeProjection> getStudyTimeInWeek(Long userId) {
        return studyJpaRepository.findStudyTimeByUserInWeek(userId);
    }

    @Override
    public Optional<StudyEntity> getStudy(UserEntity user, RecordEntity record) {
        return studyJpaRepository.findByCreatedTime(user, record);
    }
}
