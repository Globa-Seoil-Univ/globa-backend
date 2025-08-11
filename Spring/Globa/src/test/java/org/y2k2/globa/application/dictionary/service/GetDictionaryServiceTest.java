package org.y2k2.globa.application.dictionary.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import net.jqwik.api.Arbitraries;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.dictionary.dto.response.ResponseDictionaryDto;
import org.y2k2.globa.domain.dictionary.repository.DictionaryRepository;
import org.y2k2.globa.infrastructure.persistence.dictionary.entity.DictionaryEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GetDictionaryServiceTest {
    @InjectMocks
    private GetDictionaryService getDictionaryService;

    @Mock
    private DictionaryRepository dictionaryRepository;

    @Test
    @DisplayName("단어 사전 조회 - 성공")
    public void getDictionary_Success() {
        String keyword = "test";

        List<DictionaryEntity> dictionaryDtos = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(DictionaryEntity.class)
                .set("word", Arbitraries.strings().withCharRange('가', '힣').ofLength(2))
                .set("engWord", Arbitraries.strings().withCharRange('a', 'z').ofLength(5))
                .set("description", Arbitraries.strings().withCharRange('a', 'z').ofLength(60))
                .set("category", Arbitraries.strings().withCharRange('가', '힣').ofLength(2))
                .set("pronunciation", Arbitraries.strings().withCharRange('가', '힣').ofLength(5))
                .sampleList(1);
        Mockito
                .when(dictionaryRepository.getWords(keyword))
                .thenReturn(dictionaryDtos);

        ResponseDictionaryDto response = getDictionaryService.get(keyword);

        Assertions
                .assertThat(response.dictionary())
                .hasSize(1)
                .allSatisfy(dictionary -> {
                    Assertions.assertThat(dictionary.word()).isNotBlank();
                    Assertions.assertThat(dictionary.engWord()).isNotBlank();
                    Assertions.assertThat(dictionary.description()).isNotBlank();
                    Assertions.assertThat(dictionary.category()).isNotBlank();
                    Assertions.assertThat(dictionary.pronunciation()).isNotBlank();
                });

        Mockito
                .verify(dictionaryRepository, Mockito.times(1))
                .getWords(keyword);
    }
}
