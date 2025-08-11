package org.y2k2.globa.application.foldershare.usecase;

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
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class VerifyFolderWritableUseCaseTest {
    @InjectMocks
    private VerifyFolderWritableUseCase verifyFolderWritableUseCase;

    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("폴더 편집 권한 확인 - 성공")
    void isWritableTrue() {
        VerifyFolderCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(VerifyFolderCommand.class);

        Mockito
                .when(folderShareRepository.isWritable(command.userId(), command.folderId()))
                .thenReturn(true);

        verifyFolderWritableUseCase.execute(command);

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .isWritable(command.userId(), command.folderId());
    }

    @Test
    @DisplayName("폴더 편집 권한 확인 - 실패")
    void isWritableFalse() {
        VerifyFolderCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(VerifyFolderCommand.class);

        Mockito
                .when(folderShareRepository.isWritable(command.userId(), command.folderId()))
                .thenReturn(false);

        Assertions
                .assertThatThrownBy(() -> verifyFolderWritableUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_DESERVE_WRITEABLE);

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .isWritable(command.userId(), command.folderId());
    }
}
