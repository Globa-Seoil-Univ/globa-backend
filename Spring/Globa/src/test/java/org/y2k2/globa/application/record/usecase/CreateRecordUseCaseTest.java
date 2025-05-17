package org.y2k2.globa.application.record.usecase;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.record.command.CreateRecordCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CreateRecordUseCaseTest {
    @InjectMocks
    private CreateRecordUseCase createRecordUseCase;

    @Mock
    FileStore fileStore;
    @Mock
    RecordRepository recordRepository;

    @Test
    @DisplayName("문서 생성 - 성공")
    void create() {
        FileDto file = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(FileDto.class);

        CreateRecordCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(CreateRecordCommand.class);

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RecordEntity.class);

        Mockito.when(fileStore.getFile(command.dto().path()))
                .thenReturn(Optional.of(file));

        Mockito.when(recordRepository.save(Mockito.any(RecordEntity.class)))
                .thenReturn(record);

        Long createdId = createRecordUseCase.execute(command);

        Assertions.assertThat(createdId).isEqualTo(record.getRecordId());

        Mockito.verify(recordRepository, Mockito.times(1))
                .save(Mockito.any(RecordEntity.class));
    }

    @Test
    @DisplayName("문서 생성 - 실패 (FB 파일 없음)")
    void createFail() {
        CreateRecordCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(CreateRecordCommand.class);

        Mockito.when(fileStore.getFile(command.dto().path()))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> createRecordUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_RECORD_FIREBASE);

        Mockito.verify(recordRepository, Mockito.times(0))
                .save(Mockito.any(RecordEntity.class));
    }
}
