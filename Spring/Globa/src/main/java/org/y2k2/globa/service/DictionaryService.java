package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.dto.ResponseDictionaryDto;
import org.y2k2.globa.entity.DictionaryEntity;
import org.y2k2.globa.mapper.DictionaryMapper;
import org.y2k2.globa.repository.DictionaryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictionaryService {
    private final DictionaryRepository dictionaryRepository;

    public ResponseDictionaryDto getDictionary(String keyword) {
        List<DictionaryEntity> dtos = dictionaryRepository.findByWordOrEngWordOrderByCreatedTimeAsc(keyword, keyword);
        return new ResponseDictionaryDto(dtos.stream()
                .map(DictionaryMapper.INSTANCE::toDictionaryDto)
                .toList());
    }
}
