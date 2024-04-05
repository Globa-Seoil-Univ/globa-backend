package org.y2k2.globa.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ResponseAnalysisDto implements Serializable {
    private List<ResponseKeywordDto> keywords;
    private List<ResponseStudyTimesDto> studyTimes;
    private List<ResponseQuizGradeDto> quizGrades;
}