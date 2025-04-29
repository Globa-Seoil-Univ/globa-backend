package org.y2k2.globa.factory.keyword;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.factory.AbstractFactory;
import org.y2k2.globa.infrastructure.persistence.keyword.repository.KeywordTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.repository.KeywordRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Import(KeywordRepositoryImpl.class)
@Component
public class KeywordFactory extends AbstractFactory<KeywordEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private KeywordTestRepositoryImpl keywordRepository;

    private String word = "word";
    private BigDecimal importance = new BigDecimal(1);
    private RecordEntity record;
    private LocalDateTime createdTime = new CustomTimestamp().getTimestamp();

    @Override
    protected KeywordEntity create() {
        return new KeywordEntity();
    }

    @Override
    protected KeywordEntity setDefaultValues(KeywordEntity entity) {
        entity.setWord(word);
        entity.setImportance(importance);
        entity.setRecord(record);
        entity.setCreatedTime(createdTime);
        return entity;
    }

    @Override
    protected KeywordEntity saveEntity(KeywordEntity entity) {
        if (keywordRepository != null) {
            return keywordRepository.save(entity);
        } else {
            throw new RuntimeException("KeywordTestRepositoryImpl is null, entity will not be persisted");
        }
    }
}
