package org.y2k2.globa.factory;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.keyword.repository.KeywordRepository;
import org.y2k2.globa.domain.quiz.repository.QuizRepository;
import org.y2k2.globa.domain.quizattemp.repository.QuizAttemptRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folder.repository.FolderRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.repository.KeywordRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.repository.QuizRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.quizattemp.repository.QuizAttemptJpaRepository;
import org.y2k2.globa.infrastructure.persistence.quizattemp.repository.QuizAttemptRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Import(QuizAttemptRepositoryImpl.class)
@Component
public class QuizAttemptFactory extends CreatorFactory<QuizAttemptEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    private QuizEntity quiz;
    private UserEntity user;
    private Boolean isCorrect;


    private LocalDateTime createdTime = new CustomTimestamp().getTimestamp();

    @Override
    protected QuizAttemptEntity create() {
        return new QuizAttemptEntity();
    }

    @Override
    protected QuizAttemptEntity setDefaultValues(QuizAttemptEntity entity) {
        entity.setQuiz(quiz);
        entity.setUser(user);
        entity.setIsCorrect(isCorrect);
        entity.setCreatedTime(createdTime);
        return entity;
    }

    @Override
    protected QuizAttemptEntity saveEntity(QuizAttemptEntity entity) {
        if (quizAttemptRepository != null) {
            return quizAttemptRepository.save(entity);
        } else {
            throw new RuntimeException("QuizAttemptRepository is null, entity will not be persisted");
        }
    }

    public QuizAttemptEntity updateAndSave(QuizAttemptEntity entity) {
        entity.setQuiz(quiz);
        entity.setUser(user);
        entity.setIsCorrect(isCorrect);
        entity.setCreatedTime(createdTime);
        return saveEntity(entity);
    }
}
