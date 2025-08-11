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
public class VerifyFolderOwnerUseCaseTest {
    @InjectMocks
    private VerifyFolderOwnerUseCase verifyFolderOwnerUseCase;

    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("특정 폴더 소유 확인 - 성공")
    void isOwnerTrue() {
        VerifyFolderCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(VerifyFolderCommand.class);

        Mockito
                .when(folderShareRepository.isOwner(command.userId(), command.folderId()))
                .thenReturn(true);

        verifyFolderOwnerUseCase.execute(command);

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .isOwner(command.userId(), command.folderId());
    }

    @Test
    @DisplayName("특정 폴더 소유 확인 - 실패")
    void isOwnerFalse() {
        VerifyFolderCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(VerifyFolderCommand.class);

        Mockito
                .when(folderShareRepository.isOwner(command.userId(), command.folderId()))
                .thenReturn(false);

        Assertions
                .assertThatThrownBy(() -> verifyFolderOwnerUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISMATCH_FOLDER_OWNER);

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .isOwner(command.userId(), command.folderId());
    }
}
