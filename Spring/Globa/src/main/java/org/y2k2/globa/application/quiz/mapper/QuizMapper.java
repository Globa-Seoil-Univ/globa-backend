package org.y2k2.globa.application.quiz.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.y2k2.globa.application.common.mapper.CustomTimestampMapper;
import org.y2k2.globa.infrastructure.persistence.quizattemp.projection.QuizGradeProjection;
import org.y2k2.globa.application.quiz.dto.common.QuizDto;
import org.y2k2.globa.application.quiz.dto.response.ResponseQuizGradeDto;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;

@Mapper(uses = {CustomTimestampMapper.class})
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
