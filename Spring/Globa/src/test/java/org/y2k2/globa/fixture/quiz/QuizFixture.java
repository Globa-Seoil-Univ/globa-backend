package org.y2k2.globa.fixture.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.quiz.QuizFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Component
public class QuizFixture extends AbstractFixture<QuizEntity> {
    @Autowired
    private QuizFactory quizFactory;

    @Override
    protected QuizEntity build() {
        return quizFactory.createAndSave();
    }

    public QuizFixture withRecord(RecordEntity folder) {
        quizFactory.setRecord(folder);
        return this;
    }
}
