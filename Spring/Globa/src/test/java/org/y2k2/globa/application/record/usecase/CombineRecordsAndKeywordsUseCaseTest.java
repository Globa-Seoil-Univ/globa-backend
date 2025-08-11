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
import org.y2k2.globa.application.record.command.CombineRecordsAndKeywordsCommand;
import org.y2k2.globa.application.record.dto.response.ResponseRecordDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordsDto;
import org.y2k2.globa.domain.keyword.repository.KeywordRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjection;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjectionImpl;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CombineRecordsAndKeywordsUseCaseTest {
    @InjectMocks
    private CombineRecordsAndKeywordsUseCase combineRecordsAndKeywordsUseCase;

    @Mock
    private KeywordRepository keywordRepository;

    @Test
    @DisplayName("문서 + 키워드 반환 - 성공")
    void combine() {
        List<Long> recordIds = IntStream.range(1, 10)
                .mapToObj(Long::valueOf)
                .toList();

        List<KeywordProjectionImpl> keywords = recordIds.stream()
                .map(recordId -> {
                    return FixtureMonkey.builder()
                            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                            .defaultNotNull(true)
                            .build()
                            .giveMeBuilder(KeywordProjectionImpl.class)
                            .set("recordId", recordId)
                            .sample();
                })
                .toList();
        List<KeywordProjection> keywordProjections = new ArrayList<>(keywords);

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(FolderEntity.class);

        List<RecordEntity> records = recordIds.stream()
                .map(recordId -> {
                    return FixtureMonkey.builder()
                            .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                            .defaultNotNull(true)
                            .build()
                            .giveMeBuilder(RecordEntity.class)
                            .set("recordId", recordId)
                            .set("folder", folder)
                            .sample();
                })
                .toList();

        CombineRecordsAndKeywordsCommand command = new CombineRecordsAndKeywordsCommand(records, (long) records.size());

        Mockito.when(keywordRepository.getAllByRecordInKeywords(command.records()))
                .thenReturn(keywordProjections);

        ResponseRecordsDto response = combineRecordsAndKeywordsUseCase.execute(command);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.records()).isNotEmpty();
        Assertions.assertThat(response.total()).isEqualTo(command.total());

        log.info("total = {}", response.total());
        log.info("record Ids = {}", recordIds);
        log.info("response Ids = {}", response.records().stream().map(ResponseRecordDto::recordId).toList());

        Assertions.assertThat(response.records())
                .allSatisfy(record -> {
                    Assertions.assertThat(record.recordId()).isIn(recordIds);
                    Assertions.assertThat(record.keywords()).isNotEmpty();
                });

        Mockito.verify(keywordRepository, Mockito.times(1))
                .getAllByRecordInKeywords(command.records());
    }

    @Test
    @DisplayName("문서 반환 - 성공 (문서 없음)")
    void combineEmpty() {
        List<RecordEntity> records = new ArrayList<>();
        CombineRecordsAndKeywordsCommand command = new CombineRecordsAndKeywordsCommand(records, 0L);

        ResponseRecordsDto response = combineRecordsAndKeywordsUseCase.execute(command);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.records()).isEmpty();
        Assertions.assertThat(response.total()).isEqualTo(command.total());

        Mockito.verify(keywordRepository, Mockito.times(0))
                .getAllByRecordInKeywords(command.records());
    }
}
