package org.y2k2.globa.application.dictionary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.dictionary.dto.response.ResponseDictionaryDto;
import org.y2k2.globa.application.dictionary.mapper.DictionaryMapper;
import org.y2k2.globa.domain.dictionary.repository.DictionaryRepository;
import org.y2k2.globa.infrastructure.persistence.dictionary.entity.DictionaryEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDictionaryService {
    private final DictionaryRepository dictionaryRepository;

    public ResponseDictionaryDto get(String keyword) {
        List<DictionaryEntity> dictionaries = dictionaryRepository.getWords(keyword);
        return new ResponseDictionaryDto(dictionaries.stream()
                .map(DictionaryMapper.INSTANCE::toDictionaryDto)
                .toList());
    }
}
