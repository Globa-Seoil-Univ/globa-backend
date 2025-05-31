package org.y2k2.globa.fixture.highlight;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.repository.HighlightTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

@Import(HighlightTestRepositoryImpl.class)
@Component
public class HighlightFixture implements Fixture<HighlightEntity> {
    @Autowired
    private HighlightTestRepositoryImpl highlightRepository;

    @Override
    public HighlightEntity save(HighlightEntity entity) {
        return highlightRepository.save(entity);
    }

    public static HighlightBuilder builder() {
        return new HighlightBuilder();
    }

    public static class HighlightBuilder {
        private Long startIndex = 0L;
        private Long endIndex = 10L;
        private SectionEntity section;

        private HighlightBuilder() {}

        public HighlightBuilder startIndex(Long startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public HighlightBuilder endIndex(Long endIndex) {
            this.endIndex = endIndex;
            return this;
        }

        public HighlightBuilder section(SectionEntity section) {
            this.section = section;
            return this;
        }

        public HighlightEntity build() {
            HighlightEntity entity = new HighlightEntity();
            entity.setStartIndex(startIndex);
            entity.setEndIndex(endIndex);
            entity.setSection(section);
            return entity;
        }
    }
}
