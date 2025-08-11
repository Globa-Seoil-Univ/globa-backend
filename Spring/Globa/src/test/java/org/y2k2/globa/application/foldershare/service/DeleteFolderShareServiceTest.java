package org.y2k2.globa.application.foldershare.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class DeleteFolderShareServiceTest {
    @InjectMocks
    private DeleteFolderShareService deleteFolderShareService;

    @Mock
    private VerifyFolderOwnerUseCase verifyFolderOwnerUseCase;
    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("초대 삭제 - 성공")
    void deleteFolderShareSuccess() {
        Long folderId = 1L;
        Long targetId = 2L;
        Long ownerId = 3L;

        FolderShareEntity folderShareEntity = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder.folderId", folderId)
                .set("ownerUser.userId", ownerId)
                .set("targetUser.userId", targetId)
                .sample();

        Mockito
                .doNothing()
                .when(verifyFolderOwnerUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(folderShareRepository.getShareInvitation(folderId, targetId))
                .thenReturn(Optional.of(folderShareEntity));

        Mockito
                .doNothing()
                .when(folderShareRepository)
                .delete(Mockito.any(FolderShareEntity.class));

        deleteFolderShareService.delete(folderId, targetId, ownerId);

        Mockito.verify(verifyFolderOwnerUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .getShareInvitation(folderId, targetId);

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .delete(Mockito.any(FolderShareEntity.class));
    }

    @Test
    @DisplayName("초대 삭제 - 실패 (소유자 삭제 시도)")
    void deleteFolderShareFailDueToSelfInvitation() {
        Long folderId = 1L;
        Long ownerId = 2L;

        FolderShareEntity folderShareEntity = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder.folderId", folderId)
                .set("ownerUser.userId", ownerId)
                .set("targetUser.userId", ownerId)
                .sample();

        Assertions
                .assertThatThrownBy(() -> deleteFolderShareService.delete(folderId, ownerId, ownerId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVITE_BAD_REQUEST);

        Mockito.verify(verifyFolderOwnerUseCase, Mockito.times(0))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(folderShareRepository, Mockito.times(0))
                .getShareInvitation(folderId, ownerId);

        Mockito.verify(folderShareRepository, Mockito.times(0))
                .delete(Mockito.any(FolderShareEntity.class));
    }

    @Test
    @DisplayName("초대 삭제 - 실패 (존재하지 않는 초대)")
    void deleteFolderShareFailDueToNotFoundShare() {
        Long folderId = 1L;
        Long targetId = 2L;
        Long ownerId = 3L;

        Mockito
                .doNothing()
                .when(verifyFolderOwnerUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(folderShareRepository.getShareInvitation(folderId, targetId))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> deleteFolderShareService.delete(folderId, targetId, ownerId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_SHARE);

        Mockito.verify(verifyFolderOwnerUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .getShareInvitation(folderId, targetId);

        Mockito.verify(folderShareRepository, Mockito.times(0))
                .delete(Mockito.any(FolderShareEntity.class));
    }
}
