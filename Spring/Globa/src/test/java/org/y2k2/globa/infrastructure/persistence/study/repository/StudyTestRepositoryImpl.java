package org.y2k2.globa.infrastructure.persistence.study.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;

@Component
@Primary
public class StudyTestRepositoryImpl extends StudyRepositoryImpl {
    private final StudyJpaRepository studyRepository;

    public StudyTestRepositoryImpl(StudyJpaRepository studyJpaRepository, StudyJpaRepository studyRepository) {
        super(studyJpaRepository);
        this.studyRepository = studyRepository;
    }

    public StudyEntity save(StudyEntity entity) {
        return studyRepository.save(entity);
    }
}
