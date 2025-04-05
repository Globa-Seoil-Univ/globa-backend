package org.y2k2.globa.application.record.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.keyword.dto.response.ResponseKeywordDto;
import org.y2k2.globa.application.keyword.mapper.KeywordMapper;
import org.y2k2.globa.application.record.command.CombineRecordsAndKeywordsCommand;
import org.y2k2.globa.application.record.dto.response.ResponseRecordsDto;
import org.y2k2.globa.application.record.mapper.RecordMapper;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.keyword.repository.KeywordRepository;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjection;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CombineRecordsAndKeywordsUseCase implements UseCase<CombineRecordsAndKeywordsCommand, ResponseRecordsDto> {
    private final KeywordRepository keywordRepository;

    @Override
    public ResponseRecordsDto execute(CombineRecordsAndKeywordsCommand command) {
        if (command.records().isEmpty()) {
            return new ResponseRecordsDto(new ArrayList<>(), 0L);
        }

        List<KeywordProjection> keywords = keywordRepository.getAllByRecordInKeywords(command.records());

        return new ResponseRecordsDto(
                command.records().stream()
                        .map(record -> {
                            return RecordMapper.INSTANCE.toResponseRecordDto(
                                    record,
                                    record.getFolder().getFolderId(),
                                    getKeywordsByRecordId(keywords, record.getRecordId())
                            );
                        }).toList(),
                command.total()
        );
    }

    private List<ResponseKeywordDto> getKeywordsByRecordId(List<KeywordProjection> keywords, Long recordId) {
        return keywords.stream()
                .filter(keyword -> keyword.getRecordId().equals(recordId))
                .map(KeywordMapper.INSTANCE::toResponseKeywordDto)
                .toList();
    }
}
