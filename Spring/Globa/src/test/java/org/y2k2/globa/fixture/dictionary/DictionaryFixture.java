package org.y2k2.globa.fixture.dictionary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.dictionary.entity.DictionaryEntity;
import org.y2k2.globa.infrastructure.persistence.dictionary.repository.DictionaryTestRepositoryImpl;

@Import(DictionaryTestRepositoryImpl.class)
@Component
public class DictionaryFixture implements Fixture<DictionaryEntity> {
    @Autowired
    private DictionaryTestRepositoryImpl dictionaryTestRepository;

    @Override
    public DictionaryEntity save(DictionaryEntity entity) {
        return dictionaryTestRepository.save(entity);
    }

    public static DictionaryBuilder builder() {
        return new DictionaryBuilder();
    }

    public static class DictionaryBuilder {
        private String word = "default";
        private String engWord = "default";
        private String pronunciation = "default";
        private String description = "default";
        private String category = "default";

        private DictionaryBuilder() {}

        public DictionaryBuilder word(String word) {
            this.word = word;
            return this;
        }

        public DictionaryBuilder engWord(String engWord) {
            this.engWord = engWord;
            return this;
        }

        public DictionaryBuilder pronunciation(String pronunciation) {
            this.pronunciation = pronunciation;
            return this;
        }

        public DictionaryBuilder description(String description) {
            this.description = description;
            return this;
        }

        public DictionaryBuilder category(String category) {
            this.category = category;
            return this;
        }

        public DictionaryEntity build() {
            DictionaryEntity entity = new DictionaryEntity();
            entity.setWord(word);
            entity.setEngWord(engWord);
            entity.setPronunciation(pronunciation);
            entity.setDescription(description);
            entity.setCategory(category);

            return entity;
        }
    }
}
