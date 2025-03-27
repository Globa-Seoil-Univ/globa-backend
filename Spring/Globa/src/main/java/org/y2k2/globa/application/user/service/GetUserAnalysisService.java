package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.keyword.dto.response.ResponseKeywordDto;
import org.y2k2.globa.application.keyword.mapper.KeywordMapper;
import org.y2k2.globa.application.quiz.dto.response.ResponseQuizGradeDto;
import org.y2k2.globa.application.quiz.mapper.QuizMapper;
import org.y2k2.globa.application.studytime.dto.response.ResponseStudyTimesDto;
import org.y2k2.globa.application.studytime.mapper.StudyTimeMapper;
import org.y2k2.globa.domain.keyword.repository.KeywordRepository;
import org.y2k2.globa.domain.quiz.repository.QuizRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.domain.study.repository.StudyRepository;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjection;
import org.y2k2.globa.infrastructure.persistence.quiz.projection.QuizGradeProjection;
import org.y2k2.globa.infrastructure.persistence.study.projection.StudyTimeProjection;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GetUserAnalysisService {
    private final RecordRepository recordRepository;
    private final StudyRepository studyRepository;
    private final QuizRepository quizRepository;
    private final KeywordRepository keywordRepository;

    public ResponseAnalysisDto getAnalysis(CustomUserDetails details) {
        List<Long> recordIds = recordRepository.getAllRecordId(details.getUserId());

        if (recordIds.isEmpty()) {
            return ResponseAnalysisDto.empty();
        }

        return new ResponseAnalysisDto(
                getResponseKeywordDto(recordIds),
                getResponseTotalStudyTimesDto(details.getUserId()),
                getResponseQuizGradeDto(details.getUserId())
        );
    }

    private List<ResponseKeywordDto> getResponseKeywordDto(List<Long> recordIds) {
        List<KeywordProjection> keywordProjections = keywordRepository.getTop10ByAllKeywords(recordIds);
        return keywordProjections.stream().map(
                KeywordMapper.INSTANCE::toResponseKeywordDto
        ).toList();
    }

    private List<ResponseStudyTimesDto> getResponseTotalStudyTimesDto(Long userId) {
        List<StudyTimeProjection> studyTimeProjections = studyRepository.getStudyTimeInWeek(userId);

        return studyTimeProjections.stream().map(
                StudyTimeMapper.INSTANCE::toResponseTotalStudyTimesDto
        ).toList();
    }

    private List<ResponseQuizGradeDto> getResponseQuizGradeDto(Long userId) {
        List<QuizGradeProjection> quizGradeProjections = quizRepository.getQuizInWeek(userId);

        return quizGradeProjections.stream().map(
                QuizMapper.INSTANCE::toResponseQuizGradeDto
        ).toList();
    }
}
