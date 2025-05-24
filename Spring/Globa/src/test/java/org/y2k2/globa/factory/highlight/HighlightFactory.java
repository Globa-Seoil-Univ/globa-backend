package org.y2k2.globa.factory.highlight;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.factory.AbstractFactory;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.repository.HighlightRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.highlight.repository.HighlightTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

@Slf4j
@Getter
@Setter
@Import(HighlightRepositoryImpl.class)
@Component
public class HighlightFactory extends AbstractFactory<HighlightEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private HighlightTestRepositoryImpl highlightRepository;

    private SectionEntity section;
    private Long startIndex = 0L;
    private Long endIndex = 1024L;

    @Override
    protected HighlightEntity create() {
        return new HighlightEntity();
    }

    @Override
    protected HighlightEntity setDefaultValues(HighlightEntity entity) {
        entity.setSection(section);
        entity.setStartIndex(startIndex);
        entity.setEndIndex(endIndex);
        entity.setCreatedTime(new CustomTimestamp().getTimestamp());
        return entity;
    }

    @Override
    protected HighlightEntity saveEntity(HighlightEntity entity) {
        if (highlightRepository != null) {
            return highlightRepository.save(entity);
        } else {
            throw new RuntimeException("HighlightTestRepositoryImpl is null, entity will not be persisted");
        }
    }
}
