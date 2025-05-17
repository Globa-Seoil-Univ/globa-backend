package org.y2k2.globa.application.record.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
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
import org.y2k2.globa.application.record.dto.response.ResponseRecordSearchDto;
import org.y2k2.globa.application.record.mapper.RecordMapper;
import org.y2k2.globa.application.user.dto.common.UserIntroDto;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.projection.RecordSearchProjection;
import org.y2k2.globa.infrastructure.persistence.record.projection.RecordSearchProjectionImpl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class SearchRecordServiceTest {
    @InjectMocks
    private SearchRecordService searchRecordService;

    @Mock
    private RecordRepository recordRepository;

    @Test
    @DisplayName("문서 검색 - 성공")
    void searchRecordSuccess() {
        String keyword = "test";
        int page = 1;
        int count = 10;
        Long userId = 1L;
        Pageable pageable = PageRequest.of(page - 1, count);

        List<RecordSearchProjection> records = IntStream.range(0, count)
                .mapToObj(i -> FixtureMonkey.builder()
                        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build()
                        .giveMeBuilder(RecordSearchProjectionImpl.class)
                        .set("recordId", (long) i)
                        .set("userId", userId)
                        .sample()
                )
                .collect(Collectors.toList());

        Mockito.when(recordRepository.getRecordByKeyword(userId, keyword, PageRequest.of(page - 1, count)))
                .thenReturn(new PageImpl<>(records, pageable, records.size()));

        ResponseRecordSearchDto response = searchRecordService.search(keyword, page, count, userId);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getRecords()).isNotEmpty();
        Assertions.assertThat(response.getTotal()).isEqualTo(records.size());

        Assertions.assertThat(response.getRecords())
                .allSatisfy(record -> {
                    Assertions.assertThat(record.getRecordId()).isNotNull();
                    Assertions.assertThat(record.getUploader()).isInstanceOf(UserIntroDto.class);
                    Assertions.assertThat(record.getUploader().getUserId()).isNotNull();
                    Assertions.assertThat(record.getFolderId()).isNotNull();
                });
    }
}
