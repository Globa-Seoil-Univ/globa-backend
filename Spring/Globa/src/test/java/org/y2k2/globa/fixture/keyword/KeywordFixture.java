package org.y2k2.globa.fixture.keyword;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.keyword.KeywordFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Component
public class KeywordFixture extends AbstractFixture<KeywordEntity> {
    @Autowired
    private KeywordFactory keywordFactory;

    @Override
    protected KeywordEntity build() {
        return keywordFactory.createAndSave();
    }

    public KeywordFixture withRecord(RecordEntity record) {
        keywordFactory.setRecord(record);
        return this;
    }
}
