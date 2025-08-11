package org.y2k2.globa.application.record.usecase;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.record.command.AggregateRecordCommand;
import org.y2k2.globa.application.section.dto.response.ResponseSectionDto;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AggregateRecordUseCaseTest {
    @InjectMocks
    private AggregateRecordUseCase aggregateRecordUseCase;

    @Test
    @DisplayName("문서 조합 - 성공")
    void aggregate() {
        List<SectionEntity> sections = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(SectionEntity.class)
                .set("sectionId", 1L)
                .sampleList(1);

        List<AnalysisEntity> analyses = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(AnalysisEntity.class)
                .set("section", sections.get(0))
                .sampleList(1);

        List<HighlightEntity> highlights = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(HighlightEntity.class)
                .set("section", sections.get(0))
                .sampleList(1);

        List<SummaryEntity> summaries = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(SummaryEntity.class)
                .set("section", sections.get(0))
                .sampleList(1);

        AggregateRecordCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(AggregateRecordCommand.class)
                .set("sections", sections)
                .set("analyses", analyses)
                .set("highlights", highlights)
                .set("summaries", summaries)
                .sample();

        List<ResponseSectionDto> response = aggregateRecordUseCase.execute(command);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.size()).isEqualTo(command.sections().size());

        Assertions.assertThat(response.get(0)).satisfies(section -> {
            Assertions.assertThat(section.sectionId()).isEqualTo(command.sections().get(0).getSectionId());
            Assertions.assertThat(section.analyses().analysisId()).isIn(command.analyses().stream()
                    .map(AnalysisEntity::getAnalysisId)
                    .toList());
            Assertions.assertThat(section.summaries().get(0).content())
                    .isIn(command.summaries().stream()
                            .map(SummaryEntity::getContent)
                            .toList());
            Assertions.assertThat(section.analyses().highlights().get(0).highlightId())
                    .isIn(command.highlights().stream()
                            .map(HighlightEntity::getHighlightId)
                            .toList());
        });
    }

    @Test
    @DisplayName("문서 조합 - 실패")
    void aggregateFail() {
        List<SectionEntity> sections = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(SectionEntity.class)
                .set("sectionId", 1L)
                .sampleList(1);

        List<AnalysisEntity> analyses = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(AnalysisEntity.class)
                .set("section", sections.get(0))
                .set("section.sectionId", 2L)
                .sampleList(1);

        AggregateRecordCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(AggregateRecordCommand.class)
                .set("sections", sections)
                .set("analyses", analyses)
                .sample();

        Assertions.assertThatThrownBy(() -> aggregateRecordUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_ANALYSIS);
    }
}