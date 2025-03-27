package org.y2k2.globa.domain.dictionary.repository;

import org.y2k2.globa.application.dictionary.dto.common.DictionaryDto;
import org.y2k2.globa.infrastructure.persistence.dictionary.entity.DictionaryEntity;

import java.util.List;

public interface DictionaryRepository {
    void deleteAll();

    void bulkInsert(List<DictionaryDto> dtos);

    List<DictionaryEntity> getWords(String word, String engWord);
}
