package org.y2k2.globa.application.record.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Find;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.application.record.usecase.FindOwnRecordUseCase;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@ExtendWith(MockitoExtension.class)
public class DeleteRecordServiceTest {
    @InjectMocks
    private DeleteRecordService deleteRecordService;

    @Mock
    VerifyFolderOwnerUseCase verifyFolderOwnerUseCase;
    @Mock
    FindOwnRecordUseCase findOwnRecordUseCase;
    @Mock
    RecordRepository recordRepository;
    @Mock
    FileStore fileStore;

    @Test
    @DisplayName("문서 삭제 - 성공")
    void delete() {
        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RecordEntity.class);

        Mockito.when(findOwnRecordUseCase.execute(Mockito.any()))
                .thenReturn(record);

        Mockito.doNothing()
                .when(verifyFolderOwnerUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.doNothing()
                .when(recordRepository)
                .delete(record);

        Mockito.doNothing()
                .when(fileStore)
                .deleteFile(record.getPath());

        deleteRecordService.delete(1L, 1L, 1L);

        Mockito.verify(findOwnRecordUseCase, Mockito.times(1))
                .execute(Mockito.any());

        Mockito.verify(verifyFolderOwnerUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(recordRepository, Mockito.times(1))
                .delete(record);

        Mockito.verify(fileStore, Mockito.times(1))
                .deleteFile(record.getPath());
    }
}
