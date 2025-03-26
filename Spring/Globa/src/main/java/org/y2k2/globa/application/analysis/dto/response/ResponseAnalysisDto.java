package org.y2k2.globa.application.analysis.dto.response;

import org.y2k2.globa.application.keyword.dto.response.ResponseKeywordDto;
import org.y2k2.globa.application.quiz.dto.response.ResponseQuizGradeDto;
import org.y2k2.globa.application.studytime.dto.response.ResponseStudyTimesDto;

import java.util.List;

public record ResponseAnalysisDto(
        List<ResponseKeywordDto> keywords,
        List<ResponseStudyTimesDto> studyTimes,
        List<ResponseQuizGradeDto> quizGrades
) {}