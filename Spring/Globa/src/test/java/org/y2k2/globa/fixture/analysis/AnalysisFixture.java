package org.y2k2.globa.fixture.analysis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.analysis.repository.AnalysisTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

@Import(AnalysisTestRepositoryImpl.class)
@Component
public class AnalysisFixture implements Fixture<AnalysisEntity> {
    @Autowired
    private AnalysisTestRepositoryImpl analysisRepository;

    public static AnalysisBuilder builder() {
        return new AnalysisBuilder();
    }

    @Override
    public AnalysisEntity save(AnalysisEntity entity) {
        return analysisRepository.save(entity);
    }

    public static class AnalysisBuilder {
        private String content = "Default analysis content";
        private SectionEntity section;

        private AnalysisBuilder() {}

        public AnalysisBuilder content(String content) {
            this.content = content;
            return this;
        }

        public AnalysisBuilder section(SectionEntity section) {
            this.section = section;
            return this;
        }

        public AnalysisEntity build() {
            AnalysisEntity analysis = new AnalysisEntity();
            analysis.setContent(content);
            analysis.setSection(section);
            return analysis;
        }
    }
}
