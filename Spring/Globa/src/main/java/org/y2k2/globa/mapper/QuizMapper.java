package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.dto.QuizDto;
import org.y2k2.globa.entity.QuizEntity;

@Mapper
public interface QuizMapper {

    QuizMapper INSTANCE = Mappers.getMapper(QuizMapper.class);

    @Mapping(source = "quizEntity.quizId", target = "quizId")
    @Mapping(source = "question", target = "question")
    @Mapping(source = "answer", target = "answer")
    QuizDto toQuizDto(QuizEntity quizEntity);
}
