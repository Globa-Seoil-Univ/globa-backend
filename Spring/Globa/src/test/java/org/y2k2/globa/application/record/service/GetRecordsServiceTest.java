package org.y2k2.globa.application.record.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.record.dto.response.ResponseRecordsByFolderDto;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GetRecordsServiceTest {
    @InjectMocks
    private GetRecordsService getRecordsService;

    @Mock
    private VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    @Mock
    private FolderRepository folderRepository;
    @Mock
    private RecordRepository recordRepository;

    @Test
    @DisplayName("폴더 내 문서 가져오기 - 성공 (소유 O)")
    void getRecords() {
        Long folderId = 1L;
        Long userId = 1L;
        int page = 1;
        int count = 10;

        Pageable pageable = PageRequest.of(page - 1, count);

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("user.userId", userId)
                .sample();

        List<RecordEntity> records = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("folder.folderId", folder.getFolderId())
                .sampleList(count);

        Mockito.when(folderRepository.getFolder(folderId))
                .thenReturn(Optional.of(folder));

        Mockito.doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.when(recordRepository.getRecordsByFolderId(folder.getFolderId(), pageable))
                .thenReturn(new PageImpl<>(records, pageable, records.size()));

        ResponseRecordsByFolderDto response = getRecordsService.get(folderId, page, count, userId);

        log.info("Response = {}", response);

        Assertions.assertThat(response.records())
                .isNotNull()
                .isNotEmpty()
                .hasSize(count);

        Assertions.assertThat(response.isOwner())
                .isTrue();

        Assertions.assertThat(response.records())
                .allSatisfy(record -> {
                    Assertions.assertThat(record.recordId()).isNotNull();
                    Assertions.assertThat(record.title()).isNotNull();
                    Assertions.assertThat(record.path()).isNotNull();
                });
    }

    @Test
    @DisplayName("폴더 내 문서 가져오기 - 성공 (소유 X)")
    void getRecordsNotOwner() {
        Long folderId = 1L;
        Long userId = 2L;
        int page = 1;
        int count = 10;

        Pageable pageable = PageRequest.of(page - 1, count);

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("user.userId", 999L)
                .sample();

        List<RecordEntity> records = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("folder.folderId", folder.getFolderId())
                .sampleList(count);

        Mockito.when(folderRepository.getFolder(folderId))
                .thenReturn(Optional.of(folder));

        Mockito.doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.when(recordRepository.getRecordsByFolderId(folder.getFolderId(), pageable))
                .thenReturn(new PageImpl<>(records, pageable, records.size()));

        ResponseRecordsByFolderDto response = getRecordsService.get(folderId, page, count, userId);

        log.info("Response = {}", response);

        Assertions.assertThat(response.records())
                .isNotNull()
                .isNotEmpty()
                .hasSize(count);

        Assertions.assertThat(response.isOwner())
                .isFalse();

        Assertions.assertThat(response.records())
                .allSatisfy(record -> {
                    Assertions.assertThat(record.recordId()).isNotNull();
                    Assertions.assertThat(record.title()).isNotNull();
                    Assertions.assertThat(record.path()).isNotNull();
                });
    }
}
