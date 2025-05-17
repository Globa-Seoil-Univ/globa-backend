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
import org.y2k2.globa.application.record.command.FindOwnRecordCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class FindOwnRecordUseCaseTest {
    @InjectMocks
    private FindOwnRecordUseCase findOwnRecordUseCase;

    @Mock
    private RecordRepository recordRepository;

    @Test
    @DisplayName("문서 조회 - 성공")
    void find() {
        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RecordEntity.class);

        FindOwnRecordCommand command = FindOwnRecordCommand.of(
                record.getUser().getUserId(),
                record.getFolder().getFolderId(),
                record.getRecordId()
        );

        Mockito.when(recordRepository.getRecord(command.recordId()))
                .thenReturn(Optional.of(record));

        RecordEntity result = findOwnRecordUseCase.execute(command);

        Assertions.assertThat(result).isEqualTo(record);
        Mockito.verify(recordRepository, Mockito.times(1))
                .getRecord(command.recordId());
    }

    @Test
    @DisplayName("문서 조회 - 실패 (소유자 불일치)")
    void findFail() {
        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RecordEntity.class);

        FindOwnRecordCommand command = FindOwnRecordCommand.of(
                999L,
                record.getFolder().getFolderId(),
                record.getRecordId()
        );

        Mockito.when(recordRepository.getRecord(command.recordId()))
                .thenReturn(Optional.of(record));

        Assertions.assertThatThrownBy(() -> findOwnRecordUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISMATCH_RECORD_OWNER);

        Mockito.verify(recordRepository, Mockito.times(1))
                .getRecord(command.recordId());
    }

    @Test
    @DisplayName("문서 조회 - 실패 (폴더 불일치)")
    void findFailFolder() {
        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(RecordEntity.class);

        FindOwnRecordCommand command = FindOwnRecordCommand.of(
                record.getUser().getUserId(),
                999L,
                record.getRecordId()
        );

        Mockito.when(recordRepository.getRecord(command.recordId()))
                .thenReturn(Optional.of(record));

        Assertions.assertThatThrownBy(() -> findOwnRecordUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISMATCH_RECORD_FOLDER);

        Mockito.verify(recordRepository, Mockito.times(1))
                .getRecord(command.recordId());
    }
}
