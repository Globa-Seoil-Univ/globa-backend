package org.y2k2.globa.factory.quiz;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.factory.AbstractFactory;
import org.y2k2.globa.infrastructure.persistence.quiz.repository.QuizTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.repository.QuizRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Import(QuizRepositoryImpl.class)
@Component
public class QuizFactory extends AbstractFactory<QuizEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private QuizTestRepositoryImpl quizRepository;

    private String question = "question";
    private Boolean answer = true;
    private RecordEntity record;
    private LocalDateTime createdTime = new CustomTimestamp().getTimestamp();

    @Override
    protected QuizEntity create() {
        return new QuizEntity();
    }

    @Override
    protected QuizEntity setDefaultValues(QuizEntity entity) {
        entity.setQuestion(question);
        entity.setAnswer(answer);
        entity.setRecord(record);
        entity.setCreatedTime(createdTime);
        return entity;
    }

    @Override
    protected QuizEntity saveEntity(QuizEntity entity) {
        if (quizRepository != null) {
            return quizRepository.save(entity);
        } else {
            throw new RuntimeException("QuizTestRepositoryImpl is null, entity will not be persisted");
        }
    }
}
