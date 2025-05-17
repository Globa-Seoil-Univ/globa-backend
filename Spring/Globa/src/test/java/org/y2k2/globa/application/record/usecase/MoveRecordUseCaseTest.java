package org.y2k2.globa.application.record.usecase;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.record.command.MoveRecordCommand;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.exception.FileUploadException;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class MoveRecordUseCaseTest {
    @InjectMocks
    private MoveRecordUseCase moveRecordUseCase;

    @Mock
    private RecordRepository recordRepository;
    @Mock
    private FileStore fileStore;

    @Test
    @DisplayName("문서 이동 - 성공")
    void moveRecord_Success() {
        MoveRecordCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .pushAssignableTypeArbitraryIntrospector(RecordEntity.class, BeanArbitraryIntrospector.INSTANCE)
                .pushAssignableTypeArbitraryIntrospector(FolderEntity.class, BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(MoveRecordCommand.class)
                .set("record.path", "folders/1/record.ogg")
                .sample();

        String oldPath = command.record().getPath();
        String newPath = "folders/" + command.folder().getFolderId() + oldPath.substring(oldPath.lastIndexOf("/"));

        log.info("oldPath = {}", oldPath);
        log.info("newPath = {}", newPath);

        Mockito.doNothing()
                .when(fileStore)
                .moveFile(oldPath, newPath);

        Mockito.when(recordRepository.save(command.record()))
                .thenReturn(command.record());

        Mockito.doNothing()
                .when(fileStore)
                .deleteFile(oldPath);

        moveRecordUseCase.execute(command);

        Mockito.verify(fileStore, Mockito.times(1))
                .moveFile(oldPath, newPath);

        Mockito.verify(recordRepository, Mockito.times(1))
                .save(command.record());

        Mockito.verify(fileStore, Mockito.times(1))
                .deleteFile(oldPath);
    }

    @Test
    @DisplayName("문서 이동 - 실패 (DB)")
    void moveRecordFailDB() {
        MoveRecordCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .pushAssignableTypeArbitraryIntrospector(RecordEntity.class, BeanArbitraryIntrospector.INSTANCE)
                .pushAssignableTypeArbitraryIntrospector(FolderEntity.class, BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(MoveRecordCommand.class)
                .set("record.path", "folders/1/record.ogg")
                .sample();

        String oldPath = command.record().getPath();
        String newPath = "folders/" + command.folder().getFolderId() + oldPath.substring(oldPath.lastIndexOf("/"));

        Mockito.doNothing()
                .when(fileStore)
                .moveFile(oldPath, newPath);

        Mockito.when(recordRepository.save(command.record()))
                .thenThrow(new RuntimeException());

        Assertions.assertThatThrownBy(() -> moveRecordUseCase.execute(command))
                .isInstanceOf(FileUploadException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_FILE_UPLOAD)
                .hasFieldOrPropertyWithValue("filePath", newPath);

        Mockito.verify(fileStore, Mockito.times(1))
                .moveFile(oldPath, newPath);

        Mockito.verify(recordRepository, Mockito.times(1))
                .save(command.record());

        Mockito.verify(fileStore, Mockito.times(0))
                .deleteFile(oldPath);
    }
}
