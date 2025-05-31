package org.y2k2.globa.fixture.study;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.study.repository.StudyRepository;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;
import org.y2k2.globa.infrastructure.persistence.study.repository.StudyRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Import(StudyRepositoryImpl.class)
@Component
public class StudyFixture implements Fixture<StudyEntity> {
    @Autowired
    private StudyRepository studyRepository;

    @Override
    public StudyEntity save(StudyEntity entity) {
        return studyRepository.save(entity);
    }

    public static StudyBuilder builder() {
        return new StudyBuilder();
    }

    public static class StudyBuilder {
        private UserEntity user;
        private RecordEntity record;
        private Long studyTime = 10L;
        private LocalDateTime createdTime;

        private StudyBuilder() {}

        public StudyBuilder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public StudyBuilder record(RecordEntity record) {
            this.record = record;
            return this;
        }

        public StudyBuilder studyTime(Long studyTime) {
            this.studyTime = studyTime;
            return this;
        }

        public StudyBuilder createdTime(LocalDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        public StudyEntity build() {
            StudyEntity study = new StudyEntity();
            study.setStudyTime(studyTime);
            study.setUser(user);
            study.setRecord(record);
            study.setCreatedTime(createdTime);
            return study;
        }
    }
}
