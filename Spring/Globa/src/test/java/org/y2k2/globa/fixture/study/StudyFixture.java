package org.y2k2.globa.fixture.study;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.study.StudyFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Component
public class StudyFixture extends AbstractFixture<StudyEntity> {
    @Autowired
    private StudyFactory studyFactory;

    @Override
    protected StudyEntity build() {
        return studyFactory.createAndSave();
    }

    public StudyFixture withUser(UserEntity user) {
        studyFactory.setUser(user);
        return this;
    }

    public StudyFixture withRecord(RecordEntity record) {
        studyFactory.setRecord(record);
        return this;
    }

    public StudyFixture withCreatedTime(LocalDateTime createdTime) {
        studyFactory.setCreatedTime(createdTime);
        return this;
    }
}
