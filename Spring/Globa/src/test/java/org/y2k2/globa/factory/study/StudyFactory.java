package org.y2k2.globa.factory.study;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.study.repository.StudyRepository;
import org.y2k2.globa.factory.CreatorFactory;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;
import org.y2k2.globa.infrastructure.persistence.study.repository.StudyRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Import(StudyRepositoryImpl.class)
@Component
public class StudyFactory extends CreatorFactory<StudyEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private StudyRepository studyRepository;

    private UserEntity user;
    private Long studyTime = 1L;
    private RecordEntity record;
    private LocalDateTime createdTime = new CustomTimestamp().getTimestamp();

    @Override
    protected StudyEntity create() {
        return new StudyEntity();
    }

    @Override
    protected StudyEntity setDefaultValues(StudyEntity entity) {
        entity.setUser(user);
        entity.setStudyTime(studyTime);
        entity.setRecord(record);
        entity.setCreatedTime(createdTime);
        return entity;
    }

    @Override
    protected StudyEntity saveEntity(StudyEntity entity) {
        if (studyRepository != null) {
            return studyRepository.save(entity);
        } else {
            throw new RuntimeException("StudyTestRepositoryImpl is null, entity will not be persisted");
        }
    }
}
