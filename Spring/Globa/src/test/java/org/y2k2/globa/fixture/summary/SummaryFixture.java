package org.y2k2.globa.fixture.summary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.summary.SummaryFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

@Component
public class SummaryFixture extends AbstractFixture<SummaryEntity> {
    @Autowired
    private SummaryFactory summaryFactory;

    @Override
    protected SummaryEntity build() {
        return summaryFactory.createAndSave();
    }

    public SummaryFixture withContent(String content) {
        summaryFactory.setContent(content);
        return this;
    }

    public SummaryFixture withSection(SectionEntity section) {
        summaryFactory.setSection(section);
        return this;
    }
}
