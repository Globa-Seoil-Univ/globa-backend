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
import org.y2k2.globa.application.record.dto.response.ResponseRecordsDto;
import org.y2k2.globa.application.record.usecase.CombineRecordsAndKeywordsUseCase;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.List;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GetReceivingRecordsServiceTest {
    @InjectMocks
    private GetReceivingRecordsService getReceivingRecordsService;

    @Mock
    private CombineRecordsAndKeywordsUseCase combineRecordsAndKeywordsUseCase;
    @Mock
    private RecordRepository recordRepository;

    @Test
    @DisplayName("공유 받는 문서 조회 - 성공")
    void getReceivingRecord() {
        int page = 1;
        int count = 10;
        Long userId = 1L;
        Pageable pageable = PageRequest.of(page - 1, count);

        List<RecordEntity> records = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMe(RecordEntity.class, count);

        ResponseRecordsDto combinedRecords = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(ResponseRecordsDto.class)
                .set("records", records)
                .set("total", (long) records.size())
                .sample();

        Mockito.when(recordRepository.getInvitedRecord(userId, pageable))
                .thenReturn(new PageImpl<>(records, pageable, records.size()));

        Mockito.when(combineRecordsAndKeywordsUseCase.execute(Mockito.any(CombineRecordsAndKeywordsCommand.class)))
                .thenReturn(combinedRecords);

        ResponseRecordsDto response = getReceivingRecordsService.get(page, count, userId);

        log.info("Response = {}", response);

        Assertions.assertThat(response)
                .isNotNull()
                .isEqualTo(combinedRecords);

        Assertions.assertThat(response.total()).isEqualTo(records.size());
        Assertions.assertThat(response.records()).isNotEmpty();
    }
}
