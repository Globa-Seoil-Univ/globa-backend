package org.y2k2.globa.application.record.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.analysis.command.GetAnalysisCommand;
import org.y2k2.globa.application.analysis.usecase.GetAnalysisUseCase;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.record.command.AggregateRecordCommand;
import org.y2k2.globa.application.record.dto.response.ResponseRecordDetailDto;
import org.y2k2.globa.application.record.usecase.AggregateRecordUseCase;
import org.y2k2.globa.application.section.command.GetSectionsCommand;
import org.y2k2.globa.application.section.dto.response.ResponseSectionDto;
import org.y2k2.globa.application.section.usecase.GetSectionsUseCase;
import org.y2k2.globa.application.summary.command.GetSummariesCommand;
import org.y2k2.globa.application.summary.usecase.GetSummariesUseCase;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

import java.util.List;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GetRecordServiceTest {
    @InjectMocks
    private GetRecordService getRecordService;

    @Mock
    private VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    @Mock
    private GetSectionsUseCase getSectionsUseCase;
    @Mock
    private GetAnalysisUseCase getAnalysisUseCase;
    @Mock
    private GetSummariesUseCase getSummariesUseCase;
    @Mock
    private AggregateRecordUseCase aggregateRecordUseCase;
    @Mock
    private RecordRepository recordRepository;
    @Mock
    private HighlightRepository highlightRepository;

    Long recordId;
    RecordEntity record;
    List<SectionEntity> sections;
    List<AnalysisEntity> analyses;
    List<SummaryEntity> summaries;
    List<HighlightEntity> highlights;

    @BeforeEach
    void setup() {
        recordId = 1L;

        record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", recordId)
                .set("isShare", false)
                .sample();

        sections = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(SectionEntity.class)
                .set("record", record)
                .sampleList(3);

        analyses = sections.stream()
                .map(section -> FixtureMonkey.builder()
                        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build()
                        .giveMeBuilder(AnalysisEntity.class)
                        .set("section", section)
                        .sample())
                .toList();

        summaries = sections.stream()
                .map(section -> FixtureMonkey.builder()
                        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build()
                        .giveMeBuilder(SummaryEntity.class)
                        .set("section", section)
                        .sample())
                .toList();

        highlights = sections.stream()
                .map(section -> FixtureMonkey.builder()
                        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build()
                        .giveMeBuilder(HighlightEntity.class)
                        .set("section", section)
                        .sample())
                .toList();
    }

    @Test
    @DisplayName("문서 상세 조회 - 성공 (공유 문서 X)")
    void getRecordDetail() {
        Long folderId = 1L;
        Long userId = 1L;

        AggregateRecordUseCase usecase = new AggregateRecordUseCase();
        // 단순 데이터(객체) 조합 코드이기 때문에 사용
        List<ResponseSectionDto> combinedRecords = usecase.execute(
                AggregateRecordCommand.of(
                        sections,
                        analyses,
                        highlights,
                        summaries
                )
        );

        Mockito.when(recordRepository.getRecord(recordId))
                .thenReturn(java.util.Optional.of(record));

        Mockito.doNothing()
                .when(verifyFolderAccessibleUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.when(getSectionsUseCase.execute(Mockito.any(GetSectionsCommand.class)))
                .thenReturn(sections);
        Mockito.when(getAnalysisUseCase.execute(Mockito.any(GetAnalysisCommand.class)))
                .thenReturn(analyses);
        Mockito.when(getSummariesUseCase.execute(Mockito.any(GetSummariesCommand.class)))
                .thenReturn(summaries);

        Mockito.when(highlightRepository.getAllHighlights(sections.stream().map(SectionEntity::getSectionId).toList()))
                .thenReturn(highlights);

        Mockito.when(aggregateRecordUseCase.execute(Mockito.any(AggregateRecordCommand.class)))
                .thenReturn(combinedRecords);

        ResponseRecordDetailDto response = getRecordService.get(folderId, recordId, userId);

        log.info("Response = {}", response);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.recordId()).isEqualTo(recordId);
        Assertions.assertThat(response.folder().title()).isEqualTo(record.getFolder().getTitle());

        Assertions.assertThat(response.sections()).isNotNull();
        Assertions.assertThat(response.sections().size()).isEqualTo(sections.size());
        Assertions.assertThat(response.sections().getFirst())
                .satisfies(section -> {
                    Assertions.assertThat(section.sectionId()).isIn(sections.get(0).getSectionId());
                    Assertions.assertThat(section.title()).isIn(sections.get(0).getTitle());
                    Assertions.assertThat(section.analyses()).isNotNull();
                    Assertions.assertThat(section.analyses().highlights()).isNotEmpty();
                    Assertions.assertThat(section.summaries()).isNotEmpty();
                });

        Mockito.verify(verifyFolderAccessibleUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));
    }

    @Test
    @DisplayName("문서 상세 조회 - 성공 (공유 문서 O)")
    void getRecordDetailWithShare() {
        Long folderId = 1L;
        Long userId = 1L;

        record.setIsShare(true);

        AggregateRecordUseCase usecase = new AggregateRecordUseCase();
        // 단순 데이터(객체) 조합 코드이기 때문에 사용
        List<ResponseSectionDto> combinedRecords = usecase.execute(
                AggregateRecordCommand.of(
                        sections,
                        analyses,
                        highlights,
                        summaries
                )
        );

        Mockito.when(recordRepository.getRecord(recordId))
                .thenReturn(java.util.Optional.of(record));

        Mockito.when(getSectionsUseCase.execute(Mockito.any(GetSectionsCommand.class)))
                .thenReturn(sections);
        Mockito.when(getAnalysisUseCase.execute(Mockito.any(GetAnalysisCommand.class)))
                .thenReturn(analyses);
        Mockito.when(getSummariesUseCase.execute(Mockito.any(GetSummariesCommand.class)))
                .thenReturn(summaries);

        Mockito.when(highlightRepository.getAllHighlights(sections.stream().map(SectionEntity::getSectionId).toList()))
                .thenReturn(highlights);

        Mockito.when(aggregateRecordUseCase.execute(Mockito.any(AggregateRecordCommand.class)))
                .thenReturn(combinedRecords);

        ResponseRecordDetailDto response = getRecordService.get(folderId, recordId, userId);

        log.info("Response = {}", response);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.recordId()).isEqualTo(recordId);
        Assertions.assertThat(response.folder().title()).isEqualTo(record.getFolder().getTitle());

        Assertions.assertThat(response.sections()).isNotNull();
        Assertions.assertThat(response.sections().size()).isEqualTo(sections.size());
        Assertions.assertThat(response.sections().getFirst())
                .satisfies(section -> {
                    Assertions.assertThat(section.sectionId()).isIn(sections.get(0).getSectionId());
                    Assertions.assertThat(section.title()).isIn(sections.get(0).getTitle());
                    Assertions.assertThat(section.analyses()).isNotNull();
                    Assertions.assertThat(section.analyses().highlights()).isNotEmpty();
                    Assertions.assertThat(section.summaries()).isNotEmpty();
                });

        Mockito.verify(verifyFolderAccessibleUseCase, Mockito.times(0))
                .execute(Mockito.any(VerifyFolderCommand.class));
    }
}
