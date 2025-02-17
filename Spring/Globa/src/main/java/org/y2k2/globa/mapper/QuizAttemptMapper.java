package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.entity.QuizAttemptEntity;
import org.y2k2.globa.entity.QuizEntity;
import org.y2k2.globa.entity.UserEntity;

@Mapper
public interface QuizAttemptMapper {
    QuizAttemptMapper INSTANCE = Mappers.getMapper(QuizAttemptMapper.class);

    @Mapping(source = "quiz", target = "quiz")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "isCorrect", target = "isCorrect")
    @Mapping(target = "createdTime", ignore = true)
    QuizAttemptEntity toEntity(QuizEntity quiz, UserEntity user, boolean isCorrect);
}
