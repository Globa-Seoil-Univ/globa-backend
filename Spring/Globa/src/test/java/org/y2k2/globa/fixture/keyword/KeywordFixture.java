package org.y2k2.globa.fixture.keyword;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.repository.KeywordTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.math.BigDecimal;

@Import(KeywordTestRepositoryImpl.class)
@Component
public class KeywordFixture implements Fixture<KeywordEntity> {
    @Autowired
    private KeywordTestRepositoryImpl keywordRepository;

    @Override
    public KeywordEntity save(KeywordEntity entity) {
        return keywordRepository.save(entity);
    }

    public static KeywordBuilder builder() {
        return new KeywordBuilder();
    }

    public static class KeywordBuilder {
        private RecordEntity record;
        private String word = "paris";

        private KeywordBuilder() {}

        public KeywordBuilder record(RecordEntity record) {
            this.record = record;
            return this;
        }

        public KeywordBuilder word(String word) {
            this.word = word;
            return this;
        }

        public KeywordEntity build() {
            KeywordEntity entity = new KeywordEntity();
            entity.setRecord(record);
            entity.setWord(word);
            entity.setImportance(new BigDecimal("0.5"));
            return entity;
        }
    }
}
