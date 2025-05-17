package org.y2k2.globa.application.record.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.domain.keyword.repository.KeywordRepository;
import org.y2k2.globa.domain.quizattemp.repository.QuizAttemptRepository;
import org.y2k2.globa.domain.study.repository.StudyRepository;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjection;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjectionImpl;
import org.y2k2.globa.infrastructure.persistence.quiz.projection.QuizGradeProjectionImpl;
import org.y2k2.globa.infrastructure.persistence.quizattemp.projection.QuizGradeProjection;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class GetAnalysisServiceTest {
    @InjectMocks
    private GetAnalysisService getAnalysisService;

    @Mock
    VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    @Mock
    StudyRepository studyRepository;
    @Mock
    QuizAttemptRepository quizAttemptRepository;
    @Mock
    KeywordRepository keywordRepository;

    @Test
    @DisplayName("문서에 대한 분석 정보 가져오기 - 성공")
    void getAnalysis() {
        Long recordId = 1L;
        Long folderId = 1L;
        Long userId = 1L;

        List<StudyEntity> studies = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMe(StudyEntity.class, 1);

        QuizGradeProjectionImpl quiz = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(QuizGradeProjectionImpl.class);

        KeywordProjectionImpl keyword = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(KeywordProjectionImpl.class);

        List<QuizGradeProjection> quizzes = Stream.of(quiz).collect(Collectors.toList());
        List<KeywordProjection> keywords = Stream.of(keyword).collect(Collectors.toList());

        Mockito.doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.when(studyRepository.getAllStudies(userId, recordId))
                .thenReturn(studies);

        Mockito.when(quizAttemptRepository.getQuizAttemptByUserAndRecordId(userId, recordId))
                .thenReturn(quizzes);

        Mockito.when(keywordRepository.getTop10ByRecordKeywords(recordId))
                .thenReturn(keywords);

        ResponseAnalysisDto response = getAnalysisService.get(recordId, folderId, userId);

        Assertions.assertThat(response).isNotNull();

        Assertions.assertThat(response)
                .satisfies(r -> {
                    Assertions.assertThat(r.studyTimes()).isNotNull();
                    Assertions.assertThat(r.studyTimes().getFirst().studyTime()).isEqualTo(studies.getFirst().getStudyTime());

                    Assertions.assertThat(r.quizGrades()).isNotNull();
                    Assertions.assertThat(r.quizGrades().getFirst().quizGrade()).isEqualTo(quizzes.getFirst().getQuizGrade());

                    Assertions.assertThat(r.keywords()).isNotNull();
                    Assertions.assertThat(r.keywords().getFirst().word()).isEqualTo(keywords.getFirst().getWord());
                    Assertions.assertThat(r.keywords().getFirst().importance()).isEqualTo(keywords.getFirst().getImportance());
                });
    }

    @Test
    @DisplayName("문서에 대한 분석 정보 가져오기 - 성공 (기록 없음)")
    void getAnalysisWithoutRecord() {
        Long recordId = 1L;
        Long folderId = 1L;
        Long userId = 1L;

        List<StudyEntity> studies = new ArrayList<>();
        List<QuizGradeProjection> quizzes = new ArrayList<>();
        List<KeywordProjection> keywords = new ArrayList<>();

        Mockito.doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.when(studyRepository.getAllStudies(userId, recordId))
                .thenReturn(studies);

        Mockito.when(quizAttemptRepository.getQuizAttemptByUserAndRecordId(userId, recordId))
                .thenReturn(quizzes);

        Mockito.when(keywordRepository.getTop10ByRecordKeywords(recordId))
                .thenReturn(keywords);

        ResponseAnalysisDto response = getAnalysisService.get(recordId, folderId, userId);

        Assertions.assertThat(response).isNotNull();

        Assertions.assertThat(response)
                .satisfies(r -> {
                    Assertions.assertThat(r.studyTimes()).isNotNull();
                    Assertions.assertThat(r.studyTimes()).isEmpty();

                    Assertions.assertThat(r.quizGrades()).isNotNull();
                    Assertions.assertThat(r.quizGrades()).isEmpty();

                    Assertions.assertThat(r.keywords()).isNotNull();
                    Assertions.assertThat(r.keywords()).isEmpty();
                });
    }
}
