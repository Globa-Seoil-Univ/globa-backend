package org.y2k2.globa.factory.analysis;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.analysis.repository.AnalysisRepository;
import org.y2k2.globa.factory.AbstractFactory;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.analysis.repository.AnalysisRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.analysis.repository.AnalysisTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

@Slf4j
@Getter
@Setter
@Import(AnalysisRepositoryImpl.class)
@Component
public class AnalysisFactory extends AbstractFactory<AnalysisEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private AnalysisTestRepositoryImpl analysisRepository;

    private String content = "content";
    private SectionEntity section;

    @Override
    protected AnalysisEntity create() {
        return new AnalysisEntity();
    }

    @Override
    protected AnalysisEntity setDefaultValues(AnalysisEntity entity) {
        entity.setContent(content);
        entity.setSection(section);
        entity.setCreatedTime(new CustomTimestamp().getTimestamp());
        return entity;
    }

    @Override
    protected AnalysisEntity saveEntity(AnalysisEntity entity) {
        if (analysisRepository != null) {
            return analysisRepository.save(entity);
        } else {
            throw new RuntimeException("AnalysisTestRepositoryImpl is null, entity will not be persisted");
        }
    }
}
