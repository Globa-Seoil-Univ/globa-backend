package org.y2k2.globa.application.quizattempt.service;

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
import org.y2k2.globa.application.quiz.dto.request.RequestQuizDto;
import org.y2k2.globa.application.quizattemp.service.CreateQuizAttemptsService;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.quiz.repository.QuizRepository;
import org.y2k2.globa.domain.quizattemp.repository.QuizAttemptRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CreateQuizAttemptsServiceTest {
    @InjectMocks
    private CreateQuizAttemptsService createQuizAttemptsService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;

    @Mock
    private RecordRepository recordRepository;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Test
    @DisplayName("퀴즈 기록 생성 - 성공")
    void createQuizAttempts_Success() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(UserEntity.class);

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("user", user)
                .sample();

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("user", user)
                .set("folder", folder)
                .sample();

        List<QuizEntity> quizzes = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizEntity.class)
                .set("record", record)
                .sampleList(3);

        List<RequestQuizDto.Quiz> quizDtos = quizzes.stream()
                .map(quiz -> new RequestQuizDto.Quiz(quiz.getQuizId(), true))
                .toList();

        RequestQuizDto requestQuizDto = new RequestQuizDto();
        requestQuizDto.setQuizzes(quizDtos);

        Mockito
                .doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito
                .when(recordRepository.getRecord(record.getRecordId()))
                .thenReturn(Optional.of(record));

        Mockito
                .when(quizRepository.getAllByQuizzesInRecord(Mockito.any(RecordEntity.class), Mockito.anyList()))
                .thenReturn(quizzes);

        Mockito
                .doNothing()
                .when(quizAttemptRepository)
                .saveAll(Mockito.anyList());

        createQuizAttemptsService.create(
                folder.getFolderId(),
                record.getRecordId(),
                requestQuizDto,
                user.getUserId()
        );

        Mockito
                .verify(verifyFolderAccessibleUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(recordRepository, Mockito.times(1))
                .getRecord(record.getRecordId());

        Mockito
                .verify(quizRepository, Mockito.times(1))
                .getAllByQuizzesInRecord(Mockito.any(RecordEntity.class), Mockito.anyList());

        Mockito
                .verify(quizAttemptRepository, Mockito.times(1))
                .saveAll(Mockito.anyList());
    }

    @Test
    @DisplayName("퀴즈 기록 생성 - 실패 (문서 X)")
    void createQuizAttempts_Fail_NotFoundRecord() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(UserEntity.class);

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("user", user)
                .sample();

        RequestQuizDto requestQuizDto = new RequestQuizDto();
        requestQuizDto.setQuizzes(List.of(new RequestQuizDto.Quiz(1L, true)));

        Mockito
                .doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito
                .when(recordRepository.getRecord(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(
                        () -> createQuizAttemptsService.create(
                                folder.getFolderId(),
                                1L,
                                requestQuizDto,
                                user.getUserId()
                        )
                )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.NOT_FOUND_RECORD
                );

        Mockito
                .verify(verifyFolderAccessibleUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(recordRepository, Mockito.times(1))
                .getRecord(Mockito.anyLong());

        Mockito
                .verify(quizRepository, Mockito.never())
                .getAllByQuizzesInRecord(Mockito.any(RecordEntity.class), Mockito.anyList());

        Mockito
                .verify(quizAttemptRepository, Mockito.never())
                .saveAll(Mockito.anyList());
    }

    @Test
    @DisplayName("퀴즈 기록 생성 - 실패 (퀴즈 ID 불일치)")
    void createQuizAttempts_Fail_MismatchQuizRecordId() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(UserEntity.class);

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("user", user)
                .sample();

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("user", user)
                .set("folder", folder)
                .sample();

        List<QuizEntity> quizzes = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(QuizEntity.class)
                .set("record", record)
                .sampleList(3);

        RequestQuizDto requestQuizDto = new RequestQuizDto();
        requestQuizDto.setQuizzes(List.of(new RequestQuizDto.Quiz(999L, true)));

        Mockito
                .doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito
                .when(recordRepository.getRecord(record.getRecordId()))
                .thenReturn(Optional.of(record));

        Mockito
                .when(quizRepository.getAllByQuizzesInRecord(Mockito.any(RecordEntity.class), Mockito.anyList()))
                .thenReturn(quizzes);

        Assertions
                .assertThatThrownBy(
                        () -> createQuizAttemptsService.create(
                                folder.getFolderId(),
                                record.getRecordId(),
                                requestQuizDto,
                                user.getUserId()
                        )
                )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.MISMATCH_QUIZ_RECORD_ID
                );

        Mockito
                .verify(verifyFolderAccessibleUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());

        Mockito
                .verify(recordRepository, Mockito.times(1))
                .getRecord(record.getRecordId());

        Mockito
                .verify(quizRepository, Mockito.times(1))
                .getAllByQuizzesInRecord(Mockito.any(RecordEntity.class), Mockito.anyList());

        Mockito
                .verify(quizAttemptRepository, Mockito.never())
                .saveAll(Mockito.anyList());
    }
}
