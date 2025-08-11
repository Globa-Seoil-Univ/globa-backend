package org.y2k2.globa.application.record.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
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
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.application.record.command.FindOwnRecordCommand;
import org.y2k2.globa.application.record.command.MoveRecordCommand;
import org.y2k2.globa.application.record.dto.request.RequestRecordMoveDto;
import org.y2k2.globa.application.record.usecase.FindOwnRecordUseCase;
import org.y2k2.globa.application.record.usecase.MoveRecordUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@ExtendWith(MockitoExtension.class)
public class MoveRecordServiceTest {
    @InjectMocks
    private MoveRecordService moveRecordService;

    @Mock
    private FindOwnRecordUseCase findOwnRecordUseCase;
    @Mock
    private VerifyFolderOwnerUseCase verifyFolderOwnerUseCase;
    @Mock
    private VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    @Mock
    private MoveRecordUseCase moveRecordUseCase;
    @Mock
    private FolderRepository folderRepository;

    @Test
    @DisplayName("문서 이동 - 성공")
    void moveRecord() {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;
        RequestRecordMoveDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestRecordMoveDto.class);

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", recordId)
                .set("user.userId", userId)
                .set("folder.folderId", folderId)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", dto.targetId())
                .set("user.userId", userId)
                .sample();

        Mockito.when(findOwnRecordUseCase.execute(Mockito.any(FindOwnRecordCommand.class)))
                .thenReturn(record);
        Mockito.when(folderRepository.getFolder(dto.targetId()))
                .thenReturn(java.util.Optional.of(folder));

        Mockito.doNothing()
                .when(verifyFolderOwnerUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));
        Mockito.doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.doNothing()
                .when(moveRecordUseCase)
                .execute(Mockito.any(MoveRecordCommand.class));

        moveRecordService.move(folderId, recordId, dto, userId);

        Mockito.verify(findOwnRecordUseCase, Mockito.times(1))
                .execute(Mockito.any(FindOwnRecordCommand.class));

        Mockito.verify(folderRepository, Mockito.times(1))
                .getFolder(dto.targetId());

        Mockito.verify(verifyFolderOwnerUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(verifyFolderAccessibleUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(moveRecordUseCase, Mockito.times(1))
                .execute(Mockito.any(MoveRecordCommand.class));
    }

    @Test
    @DisplayName("문서 이동 - 실패 (폴더 X)")
    void moveRecordFailed() {
        Long folderId = 1L;
        Long recordId = 1L;
        Long userId = 1L;
        RequestRecordMoveDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestRecordMoveDto.class);

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", recordId)
                .set("user.userId", userId)
                .set("folder.folderId", folderId)
                .sample();

        Mockito.when(findOwnRecordUseCase.execute(Mockito.any(FindOwnRecordCommand.class)))
                .thenReturn(record);
        Mockito.when(folderRepository.getFolder(dto.targetId()))
                .thenReturn(java.util.Optional.empty());

        Assertions.assertThatThrownBy(() -> moveRecordService.move(folderId, recordId, dto, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_TARGET_FOLDER);

        Mockito.verify(findOwnRecordUseCase, Mockito.times(1))
                .execute(Mockito.any(FindOwnRecordCommand.class));

        Mockito.verify(folderRepository, Mockito.times(1))
                .getFolder(dto.targetId());

        Mockito.verify(verifyFolderOwnerUseCase, Mockito.times(0))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(verifyFolderAccessibleUseCase, Mockito.times(0))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(moveRecordUseCase, Mockito.times(0))
                .execute(Mockito.any(MoveRecordCommand.class));
    }
}
