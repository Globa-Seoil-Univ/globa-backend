package org.y2k2.globa.dto.response.analysis;

import org.y2k2.globa.dto.response.keyword.ResponseKeywordDto;
import org.y2k2.globa.dto.response.quiz.ResponseQuizGradeDto;
import org.y2k2.globa.dto.response.study.ResponseStudyTimesDto;

import java.util.List;

public record ResponseAnalysisDto(
        List<ResponseKeywordDto> keywords,
        List<ResponseStudyTimesDto> studyTimes,
        List<ResponseQuizGradeDto> quizGrades
) {}