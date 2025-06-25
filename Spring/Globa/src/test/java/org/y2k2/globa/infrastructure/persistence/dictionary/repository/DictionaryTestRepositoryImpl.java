package org.y2k2.globa.infrastructure.persistence.dictionary.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.y2k2.globa.infrastructure.persistence.dictionary.entity.DictionaryEntity;

@Component
@Primary
public class DictionaryTestRepositoryImpl extends DictionaryRepositoryImpl {
    private final DictionaryJpaRepository dictionaryRepository;

    public DictionaryTestRepositoryImpl(DictionaryJpaRepository dictionaryJpaRepository, JdbcTemplate jdbcTemplate) {
        super(dictionaryJpaRepository, jdbcTemplate);
        this.dictionaryRepository = dictionaryJpaRepository;
    }

    public DictionaryEntity save(DictionaryEntity entity) {
        return dictionaryRepository.save(entity);
    }
}
