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
public class VerifyFolderAccessibleUseCaseTest {
    @InjectMocks
    private VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;

    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("폴더 접근 가능 여부 확인 - 성공")
    void isAccessibleTrue() {
        VerifyFolderCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(VerifyFolderCommand.class);

        Mockito
                .when(folderShareRepository.isAccessible(command.userId(), command.folderId()))
                .thenReturn(true);

        verifyFolderAccessibleUseCase.execute(command);

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .isAccessible(command.userId(), command.folderId());
    }

    @Test
    @DisplayName("폴더 접근 가능 여부 확인 - 실패")
    void isAccessibleFalse() {
        VerifyFolderCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(VerifyFolderCommand.class);

        Mockito
                .when(folderShareRepository.isAccessible(command.userId(), command.folderId()))
                .thenReturn(false);

        Assertions
                .assertThatThrownBy(() -> verifyFolderAccessibleUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .isAccessible(command.userId(), command.folderId());
    }
}
