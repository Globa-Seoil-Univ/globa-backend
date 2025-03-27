package org.y2k2.globa.application.quizattemp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Mapper
public interface QuizAttemptMapper {
    QuizAttemptMapper INSTANCE = Mappers.getMapper(QuizAttemptMapper.class);

    @Mapping(source = "quiz", target = "quiz")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "isCorrect", target = "isCorrect")
    @Mapping(target = "createdTime", ignore = true)
    QuizAttemptEntity toEntity(QuizEntity quiz, UserEntity user, boolean isCorrect);
}
