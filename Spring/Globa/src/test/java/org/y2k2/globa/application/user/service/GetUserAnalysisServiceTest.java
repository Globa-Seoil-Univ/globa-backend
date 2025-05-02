package org.y2k2.globa.application.user.service;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.user.service.GetUserAnalysisService;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.keyword.repository.KeywordRepository;
import org.y2k2.globa.domain.quizattemp.repository.QuizAttemptRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.domain.study.repository.StudyRepository;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjection;
import org.y2k2.globa.infrastructure.persistence.quizattemp.projection.QuizGradeProjection;
import org.y2k2.globa.infrastructure.persistence.study.projection.StudyTimeProjection;

import java.util.List;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GetUserAnalysisServiceTest {
    @InjectMocks
    private GetUserAnalysisService getUserAnalysisService;

    @Mock
    private RecordRepository recordRepository;
    @Mock
    private StudyRepository studyRepository;
    @Mock
    private QuizAttemptRepository quizAttemptRepository;
    @Mock
    private KeywordRepository keywordRepository;

    @Test
    @DisplayName("분석 정보 조회 - 성공")
    void getAnalysis() {
        Long userId = 1L;

        List<Long> recordIds = List.of(1L, 2L, 3L);
        List<KeywordProjection> keywords = getKeywordProjections();
        List<StudyTimeProjection> studyTimes = getStudyTimeProjections();
        List<QuizGradeProjection> quizGrades = getQuizGradeProjections();

        Mockito.when(recordRepository.getAllRecordId(userId))
                .thenReturn(recordIds);

        Mockito.when(keywordRepository.getTop10ByAllKeywords(recordIds))
                .thenReturn(keywords);

        Mockito.when(studyRepository.getStudyTimeInWeek(userId))
                .thenReturn(studyTimes);

        Mockito.when(quizAttemptRepository.getQuizAttemptByUserInDays(userId))
                .thenReturn(quizGrades);

        ResponseAnalysisDto response = getUserAnalysisService.getAnalysis(userId);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.keywords()).isNotEmpty();
        Assertions.assertThat(response.studyTimes()).isNotEmpty();
        Assertions.assertThat(response.quizGrades()).isNotEmpty();

        Assertions.assertThat(response.keywords().get(0).word()).isEqualTo(keywords.get(0).getWord());
        Assertions.assertThat(response.keywords().get(0).importance()).isEqualTo(keywords.get(0).getImportance());

        Assertions.assertThat(response.studyTimes().get(0).studyTime()).isEqualTo(studyTimes.get(0).getTotalStudyTime());
        Assertions.assertThat(response.studyTimes().get(0).createdTime()).isEqualTo(studyTimes.get(0).getCreatedTime());

        Assertions.assertThat(response.quizGrades().get(0).quizGrade()).isEqualTo(quizGrades.get(0).getQuizGrade());
        Assertions.assertThat(response.quizGrades().get(0).createdTime()).isEqualTo(quizGrades.get(0).getCreatedTime());
    }

    @Test
    @DisplayName("분석 정보 조회 - 실패")
    void getAnalysisFail() {
        Long userId = 1L;

        Mockito.when(recordRepository.getAllRecordId(userId))
                .thenReturn(List.of());

        ResponseAnalysisDto response = getUserAnalysisService.getAnalysis(userId);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.keywords()).isEmpty();
        Assertions.assertThat(response.studyTimes()).isEmpty();
        Assertions.assertThat(response.quizGrades()).isEmpty();
    }

    private List<KeywordProjection> getKeywordProjections() {
        return List.of(
                new KeywordProjection() {
                    @Override
                    public Long getRecordId() {
                        return 1L;
                    }

                    @Override
                    public String getWord() {
                        return "keyword1";
                    }

                    @Override
                    public Double getImportance() {
                        return 0.9;
                    }
                },
                new KeywordProjection() {
                    @Override
                    public Long getRecordId() {
                        return 2L;
                    }

                    @Override
                    public String getWord() {
                        return "keyword2";
                    }

                    @Override
                    public Double getImportance() {
                        return 0.8;
                    }
                }
        );
    }

    private List<StudyTimeProjection> getStudyTimeProjections() {
        return List.of(
                new StudyTimeProjection() {
                    @Override
                    public Long getTotalStudyTime() {
                        return 120L;
                    }

                    @Override
                    public String getCreatedTime() {
                        return new CustomTimestamp().toString();
                    }
                },
                new StudyTimeProjection() {
                    @Override
                    public Long getTotalStudyTime() {
                        return 60L;
                    }

                    @Override
                    public String getCreatedTime() {
                        return new CustomTimestamp().toString();
                    }
                }
        );
    }

    private List<QuizGradeProjection> getQuizGradeProjections() {
        return List.of(
                new QuizGradeProjection() {
                    @Override
                    public Double getQuizGrade() {
                        return 90.0;
                    }

                    @Override
                    public String getCreatedTime() {
                        return new CustomTimestamp().toString();
                    }
                },
                new QuizGradeProjection() {
                    @Override
                    public Double getQuizGrade() {
                        return 85.0;
                    }

                    @Override
                    public String getCreatedTime() {
                        return new CustomTimestamp().toString();
                    }
                }
        );
    }
}
