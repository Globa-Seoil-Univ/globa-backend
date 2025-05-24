package org.y2k2.globa.factory.summary;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.summary.repository.SummaryRepository;
import org.y2k2.globa.factory.AbstractFactory;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;
import org.y2k2.globa.infrastructure.persistence.summary.repository.SummaryRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.summary.repository.SummaryTestRepositoryImpl;

@Slf4j
@Getter
@Setter
@Import(SummaryRepositoryImpl.class)
@Component
public class SummaryFactory extends AbstractFactory<SummaryEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private SummaryTestRepositoryImpl summaryRepository;

    private SectionEntity section;
    private String content = "content";

    @Override
    protected SummaryEntity create() {
        return new SummaryEntity();
    }

    @Override
    protected SummaryEntity setDefaultValues(SummaryEntity entity) {
        entity.setContent(content);
        entity.setSection(section);
        entity.setCreatedTime(new CustomTimestamp().getTimestamp());
        return entity;
    }

    @Override
    protected SummaryEntity saveEntity(SummaryEntity entity) {
        if (summaryRepository != null) {
            return summaryRepository.save(entity);
        } else {
            throw new RuntimeException("SummaryTestRepositoryImpl is null, entity will not be persisted");
        }
    }
}
