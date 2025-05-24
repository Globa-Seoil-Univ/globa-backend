package org.y2k2.globa.factory.section;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.section.repository.SectionRepository;
import org.y2k2.globa.factory.AbstractFactory;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.section.repository.SectionRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.section.repository.SectionTestRepositoryImpl;

@Slf4j
@Getter
@Setter
@Import(SectionRepositoryImpl.class)
@Component
public class SectionFactory extends AbstractFactory<SectionEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private SectionTestRepositoryImpl sectionRepository;

    private RecordEntity record;
    private String title = "title";
    private Long startTime = 0L;
    private Long endTime = 30L;

    @Override
    protected SectionEntity create() {
        return new SectionEntity();
    }

    @Override
    protected SectionEntity setDefaultValues(SectionEntity entity) {
        entity.setTitle(title);
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setRecord(record);
        entity.setCreatedTime(new CustomTimestamp().getTimestamp());

        return entity;
    }

    @Override
    protected SectionEntity saveEntity(SectionEntity entity) {
        if (sectionRepository != null) {
            return sectionRepository.save(entity);
        } else {
            throw new RuntimeException("SectionTestRepositoryImpl is null, entity will not be persisted");
        }
    }
}
