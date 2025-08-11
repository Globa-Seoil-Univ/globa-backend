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
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderWritableUseCase;
import org.y2k2.globa.application.record.command.FindOwnRecordCommand;
import org.y2k2.globa.application.record.usecase.FindOwnRecordUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UpdateShareLinkStatusServiceTest {
    @InjectMocks
    private UpdateShareLinkStatusService updateShareLinkStatusService;

    @Mock
    private FindOwnRecordUseCase findOwnRecordUseCase;
    @Mock
    private VerifyFolderWritableUseCase verifyFolderWritableUseCase;
    @Mock
    private FolderRepository folderRepository;
    @Mock
    private RecordRepository recordRepository;

    @Test
    @DisplayName("문서 공유 상태 수정 - 성공")
    void updateShareLinkStatus() {
        // given
        Long folderId = 1L;
        Long recordId = 1L;
        boolean isShared = true;
        Long userId = 1L;

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", folderId)
                .set("user.userId", userId)
                .sample();

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", recordId)
                .set("user.userId", userId)
                .set("folder.folderId", folderId)
                .set("isShare", false)
                .sample();

        Mockito.when(folderRepository.getFolder(folderId))
                .thenReturn(Optional.of(folder));

        Mockito.when(findOwnRecordUseCase.execute(Mockito.any(FindOwnRecordCommand.class)))
                .thenReturn(record);

        Mockito.doNothing()
                .when(verifyFolderWritableUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.when(recordRepository.save(Mockito.any(RecordEntity.class)))
                .thenReturn(record);

        updateShareLinkStatusService.update(folderId, recordId, isShared, userId);

        Mockito.verify(folderRepository, Mockito.times(1))
                .getFolder(folderId);

        Mockito.verify(findOwnRecordUseCase, Mockito.times(1))
                .execute(Mockito.any(FindOwnRecordCommand.class));

        Mockito.verify(verifyFolderWritableUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(recordRepository, Mockito.times(1))
                .save(Mockito.any(RecordEntity.class));
    }

    @Test
    @DisplayName("문서 공유 상태 수정 - 실패 (폴더 X)")
    void updateShareLinkStatusNotFoundFolder() {
        // given
        Long folderId = 1L;
        Long recordId = 1L;
        boolean isShared = true;
        Long userId = 1L;

        Mockito.when(folderRepository.getFolder(folderId))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> updateShareLinkStatusService.update(folderId, recordId, isShared, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_FOLDER);

        Mockito.verify(folderRepository, Mockito.times(1))
                .getFolder(folderId);

        Mockito.verify(findOwnRecordUseCase, Mockito.times(0))
                .execute(Mockito.any(FindOwnRecordCommand.class));

        Mockito.verify(recordRepository, Mockito.times(0))
                .save(Mockito.any(RecordEntity.class));
    }
}
