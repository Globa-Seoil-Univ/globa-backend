package org.y2k2.globa.fixture.section;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.section.SectionFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

@Component
public class SectionFixture extends AbstractFixture<SectionEntity> {
    @Autowired
    private SectionFactory sectionFactory;

    @Override
    protected SectionEntity build() {
        return sectionFactory.createAndSave();
    }

    public SectionFixture withTitle(String title) {
        sectionFactory.setTitle(title);
        return this;
    }

    public SectionFixture withStartTime(Long startTime) {
        sectionFactory.setStartTime(startTime);
        return this;
    }

    public SectionFixture withEndTime(Long endTime) {
        sectionFactory.setEndTime(endTime);
        return this;
    }

    public SectionFixture withRecord(RecordEntity record) {
        sectionFactory.setRecord(record);
        return this;
    }
}
