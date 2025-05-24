package org.y2k2.globa.fixture.analysis;

import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.analysis.AnalysisFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

@Component
public class AnalysisFixture extends AbstractFixture<AnalysisEntity> {
    private final AnalysisFactory analysisFactory;

    public AnalysisFixture(AnalysisFactory analysisFactory) {
        this.analysisFactory = analysisFactory;
    }

    @Override
    protected AnalysisEntity build() {
        return analysisFactory.createAndSave();
    }

    public AnalysisFixture withContent(String content) {
        analysisFactory.setContent(content);
        return this;
    }

    public AnalysisFixture withSection(SectionEntity section) {
        analysisFactory.setSection(section);
        return this;
    }
}
