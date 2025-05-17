package org.y2k2.globa.application.record.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.application.record.command.FindOwnRecordCommand;
import org.y2k2.globa.application.record.usecase.FindOwnRecordUseCase;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@ExtendWith(MockitoExtension.class)
public class UpdateRecordNameServiceTest {
    @InjectMocks
    private UpdateRecordNameService updateRecordNameService;

    @Mock
    private FindOwnRecordUseCase findOwnRecordUseCase;
    @Mock
    private VerifyFolderOwnerUseCase verifyFolderOwnerUseCase;
    @Mock
    private RecordRepository recordRepository;

    @Test
    @DisplayName("문서 이름 수정 - 성공")
    void updateNameRecord() {
        Long folderId = 1L;
        Long recordId = 1L;
        String title = "newTitle";
        Long userId = 1L;

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RecordEntity.class);

        Mockito.when(findOwnRecordUseCase.execute(Mockito.any(FindOwnRecordCommand.class)))
                .thenReturn(record);

        Mockito.doNothing()
                .when(verifyFolderOwnerUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        updateRecordNameService.update(folderId, recordId, title, userId);

        Mockito.verify(recordRepository, Mockito.times(1))
                .save(record);
    }
}
