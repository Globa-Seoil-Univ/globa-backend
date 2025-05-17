package org.y2k2.globa.application.record.service;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.application.record.command.CombineRecordsAndKeywordsCommand;
import org.y2k2.globa.application.record.dto.response.ResponseRecordDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordsDto;
import org.y2k2.globa.application.record.usecase.CombineRecordsAndKeywordsUseCase;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.List;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GetSharingRecordServiceTest {
    @InjectMocks
    private GetSharingRecordService getSharingRecordService;

    @Mock
    private CombineRecordsAndKeywordsUseCase combineRecordsAndKeywordsUseCase;
    @Mock
    private RecordRepository recordRepository;

    @Test
    @DisplayName("공유 중인 문서 - 성공")
    void getSharingRecord() {
        int page = 1;
        int count = 10;
        Long userId = 1L;

        Pageable pageable = PageRequest.of(page - 1, count);
        List<RecordEntity> records = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("isShare", false)
                .sampleList(count);

        List<Long> recordIds = records.stream()
                .map(RecordEntity::getRecordId)
                .toList();

        List<ResponseRecordDto> recordDtos = recordIds.stream()
                .map(recordId -> FixtureMonkey.builder()
                        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build()
                        .giveMeBuilder(ResponseRecordDto.class)
                        .set("recordId", recordId)
                        .sample())
                .toList();

        ResponseRecordsDto responseRecordsDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(ResponseRecordsDto.class)
                .set("records", recordDtos)
                .set("total", (long) records.size())
                .sample();

        Mockito.when(recordRepository.getOwnedRecord(userId, pageable))
                .thenReturn(new PageImpl<>(records, pageable, records.size()));

        Mockito.when(combineRecordsAndKeywordsUseCase.execute(Mockito.any(CombineRecordsAndKeywordsCommand.class)))
                .thenReturn(responseRecordsDto);

        ResponseRecordsDto response = getSharingRecordService.get(page, count, userId);
        log.info("Response = {}", response);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.records()).isNotEmpty();
        Assertions.assertThat(response.total()).isEqualTo(records.size());

        Assertions.assertThat(response.records())
                .allSatisfy(dto -> {
                    Assertions.assertThat(dto.recordId()).isNotNull();
                    Assertions.assertThat(dto.title()).isNotNull();
                    Assertions.assertThat(dto.path()).isNotNull();
                    Assertions.assertThat(dto.keywords()).isNotNull();
                    Assertions.assertThat(dto.createdTime()).isNotNull();
                });
    }
}
