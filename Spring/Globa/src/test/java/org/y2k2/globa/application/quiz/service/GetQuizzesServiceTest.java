package org.y2k2.globa.application.quiz.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.quiz.dto.response.ResponseQuizzesDto;
import org.y2k2.globa.domain.quiz.repository.QuizRepository;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GetQuizzesServiceTest {
    @InjectMocks
    private GetQuizzesService getQuizzesService;

    @Mock
    private VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    @Mock
    private QuizRepository quizRepository;

    @Test
    @DisplayName("퀴즈 목록 조회 - 성공")
    void getQuizzes_Success() {
        Long folderId = 1L,
                recordId = 1L,
                userId = 1L;

        // O/X 퀴즈 질문
        String[] questions = {
                "지구는 평평하다?",
                "태양은 지구의 중심을 돌고 있다?",
                "물은 H2O로 구성되어 있다?",
                "인간은 공룡과 함께 살았다?",
                "한국의 수도는 서울이다?"
        };

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RecordEntity.class);

        List<QuizEntity> quizzes = Arrays.stream(questions)
                .map(question -> {
                    return FixtureMonkey.builder()
                            .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                            .defaultNotNull(true)
                            .build()
                            .giveMeBuilder(QuizEntity.class)
                            .set("question", question)
                            .set("record", record)
                            .sample();
                })
                .toList();

        Mockito
                .doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(quizRepository.getAllQuizzes(recordId))
                .thenReturn(quizzes);

        ResponseQuizzesDto response = getQuizzesService.get(folderId, recordId, userId);

        Assertions
                .assertThat(response.quizzes())
                .hasSize(5)
                .allSatisfy(quizDto -> {
                    Assertions.assertThat(quizDto.quizId()).isNotNull();
                    Assertions.assertThat(quizDto.question()).isNotBlank();
                    // boolean 값 확인
                    Assertions
                            .assertThat(quizDto.answer())
                            .isNotNull()
                            .isInstanceOf(Boolean.class);
                });

        Mockito.verify(verifyFolderAccessibleUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(quizRepository, Mockito.times(1))
                .getAllQuizzes(recordId);
    }

    @Test
    @DisplayName("퀴즈 목록 조회 - 성공 (퀴즈 없음)")
    void getQuizzes_Empty() {
        Long folderId = 1L,
                recordId = 1L,
                userId = 1L;

        Mockito
                .doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(quizRepository.getAllQuizzes(recordId))
                .thenReturn(List.of());

        ResponseQuizzesDto response = getQuizzesService.get(folderId, recordId, userId);

        Assertions
                .assertThat(response.quizzes())
                .isEmpty();

        Mockito.verify(verifyFolderAccessibleUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(quizRepository, Mockito.times(1))
                .getAllQuizzes(recordId);
    }
}
