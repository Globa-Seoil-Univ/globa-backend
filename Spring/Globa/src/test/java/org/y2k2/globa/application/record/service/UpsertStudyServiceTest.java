package org.y2k2.globa.application.record.service;

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
import org.y2k2.globa.application.study.command.UpsertStudyCommand;
import org.y2k2.globa.application.study.dto.request.RequestStudyDto;
import org.y2k2.globa.application.study.usecase.UpsertStudyUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UpsertStudyServiceTest {
    @InjectMocks
    private UpsertStudyService upsertStudyService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private UpsertStudyUseCase upsertStudyUseCase;
    @Mock
    private VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    @Mock
    private RecordRepository recordRepository;

    @Test
    @DisplayName("공부 시간 수정 - 성공")
    void upsertStudy() {
        // given
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;
        RequestStudyDto dto = new RequestStudyDto(60L);

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .sample();

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("user.userId", userId)
                .set("recordId", recordId)
                .set("folder.folderId", folderId)
                .sample();

        Mockito.when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito.when(recordRepository.getRecord(recordId))
                .thenReturn(Optional.of(record));

        Mockito.doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.doNothing()
                .when(upsertStudyUseCase)
                .execute(Mockito.any(UpsertStudyCommand.class));

        upsertStudyService.upsert(folderId, recordId, dto, userId);

        Mockito.verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito.verify(recordRepository, Mockito.times(1))
                .getRecord(recordId);

        Mockito.verify(verifyFolderAccessibleUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(upsertStudyUseCase, Mockito.times(1))
                .execute(Mockito.any(UpsertStudyCommand.class));
    }

    @Test
    @DisplayName("공부 시간 수정 - 실패 (문서 X)")
    void upsertStudyNotFoundRecord() {
        // given
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;
        RequestStudyDto dto = new RequestStudyDto(60L);

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .sample();

        Mockito.when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito.when(recordRepository.getRecord(recordId))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> upsertStudyService.upsert(folderId, recordId, dto, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_RECORD);

        Mockito.verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito.verify(recordRepository, Mockito.times(1))
                .getRecord(recordId);

        Mockito.verify(verifyFolderAccessibleUseCase, Mockito.times(0))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(upsertStudyUseCase, Mockito.times(0))
                .execute(Mockito.any(UpsertStudyCommand.class));
    }

    @Test
    @DisplayName("공부 시간 수정 - 실패 (폴더 ID 불일치)")
    void upsertStudyMisMatchFolderId() {
        // given
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;
        RequestStudyDto dto = new RequestStudyDto(60L);

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .sample();

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("user.userId", userId)
                .set("recordId", recordId)
                // 폴더 ID를 다르게 설정
                .set("folder.folderId", 999L)
                .sample();

        Mockito.when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito.when(recordRepository.getRecord(recordId))
                .thenReturn(Optional.of(record));

        Assertions.assertThatThrownBy(() -> upsertStudyService.upsert(folderId, recordId, dto, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISMATCH_RECORD_FOLDER);

        Mockito.verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito.verify(recordRepository, Mockito.times(1))
                .getRecord(recordId);

        Mockito.verify(verifyFolderAccessibleUseCase, Mockito.times(0))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(upsertStudyUseCase, Mockito.times(0))
                .execute(Mockito.any(UpsertStudyCommand.class));
    }
}
