package org.y2k2.globa.application.dictionary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.userrole.usecase.VerifyUserWritableUseCase;
import org.y2k2.globa.common.util.Excel;
import org.y2k2.globa.domain.dictionary.repository.DictionaryRepository;

@Service
@RequiredArgsConstructor
public class CreateDictionaryService {
    private final Excel excel;

    private final VerifyUserWritableUseCase verifyUserWritableUseCase;

    private final DictionaryRepository dictionaryRepository;

    @Transactional
    public void create(Long userId) {
        verifyUserWritableUseCase.execute(userId);

        dictionaryRepository.truncate();
        dictionaryRepository.bulkInsert(excel.getDictionaryDto());
    }
}
