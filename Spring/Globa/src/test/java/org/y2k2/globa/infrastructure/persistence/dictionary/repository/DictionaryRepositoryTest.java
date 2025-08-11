package org.y2k2.globa.infrastructure.persistence.dictionary.repository;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import net.jqwik.api.Arbitraries;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.y2k2.globa.application.dictionary.dto.common.DictionaryDto;
import org.y2k2.globa.domain.dictionary.repository.DictionaryRepository;
import org.y2k2.globa.infrastructure.persistence.config.RepositoryIntegrationTest;
import org.y2k2.globa.infrastructure.persistence.dictionary.entity.DictionaryEntity;

import java.util.List;

@Slf4j
@RepositoryIntegrationTest
public class DictionaryRepositoryTest {
    @Autowired
    private DictionaryRepository dictionaryRepository;

    @Test
    @DisplayName("사전 데이터 초기화 - 성공")
    void truncate_Success() {
        dictionaryRepository.truncate();
        log.info("Dictionary data has been successfully truncated.");
    }

    @Test
    @DisplayName("사전 데이터 삽입 - 성공")
    void bulkInsert_Success() {
        List<DictionaryDto> dictionaryDtos = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(DictionaryDto.class)
                .set("word", Arbitraries.strings().withCharRange('가', '힣').ofLength(2))
                .set("engWord", Arbitraries.strings().withCharRange('a', 'z').ofLength(5))
                .set("description", Arbitraries.strings().withCharRange('a', 'z').ofLength(60))
                .set("category", Arbitraries.strings().withCharRange('가', '힣').ofLength(2))
                .set("pronunciation", Arbitraries.strings().withCharRange('가', '힣').ofLength(5))
                .sampleList(10);

        dictionaryRepository.bulkInsert(dictionaryDtos);
        log.info("Dictionary data has been successfully inserted.");
    }

    @Test
    @DisplayName("사전 단어 검색 - 성공")
    void getWords_Success() {
        DictionaryDto dictionary = new DictionaryDto(
                "파이썬",
                "Python",
                "A high-level programming language.",
                "명사",
                "[파이썬]"
        );

        dictionaryRepository.bulkInsert(List.of(dictionary));

        List<DictionaryEntity> words = dictionaryRepository.getWords("파이썬");

        log.info("Retrieved words: {}", words);

        Assertions
                .assertThat(words)
                .isNotEmpty();

        Assertions
                .assertThat(words.get(0).getWord())
                .isEqualTo("파이썬");

        Assertions
                .assertThat(words.get(0).getEngWord())
                .isEqualTo("Python");

        Assertions
                .assertThat(words.get(0).getDescription())
                .isEqualTo("A high-level programming language.");

        Assertions
                .assertThat(words.get(0).getCategory())
                .isEqualTo("명사");

        Assertions
                .assertThat(words.get(0).getPronunciation())
                .isEqualTo("[파이썬]");
    }
}
