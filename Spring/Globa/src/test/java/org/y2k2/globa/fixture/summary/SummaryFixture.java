package org.y2k2.globa.fixture.summary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;
import org.y2k2.globa.infrastructure.persistence.summary.repository.SummaryTestRepositoryImpl;

@Import(SummaryTestRepositoryImpl.class)
@Component
public class SummaryFixture implements Fixture<SummaryEntity> {
    @Autowired
    private SummaryTestRepositoryImpl summaryRepository;

    @Override
    public SummaryEntity save(SummaryEntity entity) {
        return summaryRepository.save(entity);
    }

    public static SummaryBuilder builder() {
        return new SummaryBuilder();
    }

    public static class SummaryBuilder {
        private String content;
        private SectionEntity section;

        private SummaryBuilder() {}

        public SummaryBuilder content(String content) {
            this.content = content;
            return this;
        }

        public SummaryBuilder section(SectionEntity section) {
            this.section = section;
            return this;
        }

        public SummaryEntity build() {
            SummaryEntity summary = new SummaryEntity();
            summary.setContent(content);
            summary.setSection(section);
            return summary;
        }
    }
}
