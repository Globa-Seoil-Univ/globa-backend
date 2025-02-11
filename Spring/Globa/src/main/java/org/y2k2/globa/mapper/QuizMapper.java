package org.y2k2.globa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.Projection.QuizGradeProjection;
import org.y2k2.globa.dto.common.quiz.QuizDto;
import org.y2k2.globa.dto.response.quiz.ResponseQuizGradeDto;
import org.y2k2.globa.entity.QuizEntity;

@Mapper
public interface QuizMapper {

    QuizMapper INSTANCE = Mappers.getMapper(QuizMapper.class);

    @Mapping(source = "quizGrade", target = "quizGrade")
    @Mapping(source = "createdTime", target = "createdTime")
    ResponseQuizGradeDto toResponseQuizGradeDto(QuizGradeProjection projection);

    @Mapping(source = "quizId", target = "quizId")
    @Mapping(source = "question", target = "question")
    @Mapping(source = "answer", target = "answer")
    QuizDto toQuizDto(QuizEntity quizEntity);
}
