package org.y2k2.globa.dto.response.analysis;

import lombok.*;
import org.y2k2.globa.dto.response.keyword.ResponseKeywordDto;
import org.y2k2.globa.dto.response.study.ResponseStudyTimesDto;
import org.y2k2.globa.dto.response.quiz.ResponseQuizGradeDto;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseAnalysisDto {
    private List<ResponseKeywordDto> keywords;
    private List<ResponseStudyTimesDto> studyTimes;
    private List<ResponseQuizGradeDto> quizGrades;
}