package org.y2k2.globa.infrastructure.persistence.keyword;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.repository.KeywordJpaRepository;
import org.y2k2.globa.infrastructure.persistence.keyword.repository.KeywordRepositoryImpl;

@Component
@Primary
public class KeywordTestRepositoryImpl extends KeywordRepositoryImpl {
    private final KeywordJpaRepository keywordRepository;

    public KeywordTestRepositoryImpl(KeywordJpaRepository keywordJpaRepository, KeywordJpaRepository keywordRepository) {
        super(keywordJpaRepository);
        this.keywordRepository = keywordRepository;
    }

    public KeywordEntity save(KeywordEntity entity) {
        return keywordRepository.save(entity);
    }
}
