package org.y2k2.globa.application.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.keyword.dto.response.ResponseKeywordDto;
import org.y2k2.globa.application.keyword.mapper.KeywordMapper;
import org.y2k2.globa.application.quiz.dto.response.ResponseQuizGradeDto;
import org.y2k2.globa.application.quiz.mapper.QuizMapper;
import org.y2k2.globa.application.study.dto.response.ResponseStudyTimesDto;
import org.y2k2.globa.application.study.mapper.StudyMapper;
import org.y2k2.globa.domain.keyword.repository.KeywordRepository;
import org.y2k2.globa.domain.quizattemp.repository.QuizAttemptRepository;
import org.y2k2.globa.domain.study.repository.StudyRepository;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjection;
import org.y2k2.globa.infrastructure.persistence.quizattemp.projection.QuizGradeProjection;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAnalysisService {
    private final VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;

    private final StudyRepository studyRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final KeywordRepository keywordRepository;

    public ResponseAnalysisDto get(Long recordId, Long folderId, Long userId) {
        verifyFolderAccessibleUseCase.execute(
                VerifyFolderCommand.of(userId, folderId)
        );

        List<StudyEntity> studies = studyRepository.getAllStudies(userId, recordId);
        List<QuizGradeProjection> quizzes = quizAttemptRepository.getQuizAttemptByUserAndRecordId(userId, recordId);
        List<KeywordProjection> keywords = keywordRepository.getTop10ByRecordKeywords(recordId);

        List<ResponseStudyTimesDto> responseStudyTimes = studies.stream()
                .map(StudyMapper.INSTANCE::toResponseStudyTimesDto)
                .toList();
        List<ResponseQuizGradeDto> responseQuizGrades = quizzes.stream()
                .map(QuizMapper.INSTANCE::toResponseQuizGradeDto)
                .toList();
        List<ResponseKeywordDto> responseKeywords = keywords.stream()
                .map(KeywordMapper.INSTANCE::toResponseKeywordDto)
                .toList();

        return new ResponseAnalysisDto(responseKeywords, responseStudyTimes, responseQuizGrades);
    }
}
