package org.y2k2.globa.application.dictionary.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.userrole.usecase.VerifyUserWritableUseCase;
import org.y2k2.globa.common.util.Excel;
import org.y2k2.globa.domain.dictionary.repository.DictionaryRepository;

@ExtendWith(MockitoExtension.class)
public class CreateDictionaryServiceTest {
    @InjectMocks
    private CreateDictionaryService createDictionaryService;

    @Mock
    private Excel excel;
    @Mock
    private VerifyUserWritableUseCase verifyUserWritableUseCase;
    @Mock
    private DictionaryRepository dictionaryRepository;

    @Test
    @DisplayName("단어 사전 생성 - 성공")
    public void createDictionary_Success() {
        Long userId = 1L;

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(userId);

        Mockito
                .doNothing()
                .when(dictionaryRepository)
                .truncate();

        Mockito
                .doNothing()
                .when(dictionaryRepository)
                .bulkInsert(Mockito.anyList());

        createDictionaryService.create(userId);

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(dictionaryRepository, Mockito.times(1))
                .truncate();

        Mockito
                .verify(dictionaryRepository, Mockito.times(1))
                .bulkInsert(Mockito.anyList());
    }
}
