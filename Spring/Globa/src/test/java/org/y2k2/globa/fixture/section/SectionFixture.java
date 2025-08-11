package org.y2k2.globa.fixture.section;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.section.repository.SectionTestRepositoryImpl;

@Import(SectionTestRepositoryImpl.class)
@Component
public class SectionFixture implements Fixture<SectionEntity> {
    @Autowired
    private SectionTestRepositoryImpl sectionRepository;

    @Override
    public SectionEntity save(SectionEntity entity) {
        return sectionRepository.save(entity);
    }

    public static SectionBuilder builder() {
        return new SectionBuilder();
    }

    public static class SectionBuilder {
        private String title = "Default Section Title";
        private Long startTime = 0L;
        private Long endTime = 10L;
        private RecordEntity record;

        private SectionBuilder() {}

        public SectionBuilder title(String title) {
            this.title = title;
            return this;
        }

        public SectionBuilder startTime(Long startTime) {
            this.startTime = startTime;
            return this;
        }

        public SectionBuilder endTime(Long endTime) {
            this.endTime = endTime;
            return this;
        }

        public SectionBuilder record(RecordEntity record) {
            this.record = record;
            return this;
        }

        public SectionEntity build() {
            SectionEntity section = new SectionEntity();
            section.setTitle(title);
            section.setStartTime(startTime);
            section.setEndTime(endTime);
            section.setRecord(record);
            return section;
        }
    }
}
