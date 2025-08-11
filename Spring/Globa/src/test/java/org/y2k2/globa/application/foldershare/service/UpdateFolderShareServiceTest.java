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
import org.y2k2.globa.application.folderrole.command.FolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.FindFolderRoleUseCase;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.dto.request.RequestInviteDto;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UpdateFolderShareServiceTest {
    @InjectMocks
    private UpdateFolderShareService updateFolderShareService;

    @Mock
    private FindFolderRoleUseCase findFolderRoleUseCase;
    @Mock
    private VerifyFolderOwnerUseCase verifyFolderOwnerUseCase;
    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("폴더 권한 변경 - 성공")
    void updateFolderShareSuccess() {
        Long folderId = 1L;
        Long ownerId = 1L;
        Long targetId = 2L;
        RequestInviteDto dto = new RequestInviteDto(FolderRole.EDITOR.name());

        FolderRoleEntity folderRole = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleName", FolderRole.EDITOR)
                .sample();

        FolderShareEntity folderShare = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder.folderId", folderId)
                .set("ownerUser.userId", ownerId)
                .set("targetUser.userId", targetId)
                .set("role", folderRole)
                .sample();

        Mockito
                .doNothing()
                .when(verifyFolderOwnerUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(folderShareRepository.getShareInvitation(folderId, targetId))
                .thenReturn(Optional.of(folderShare));

        Mockito
                .when(findFolderRoleUseCase.execute(Mockito.any(FolderRoleCommand.class)))
                .thenReturn(Optional.of(folderRole));

        Mockito
                .when(folderShareRepository.save(Mockito.any(FolderShareEntity.class)))
                .thenReturn(folderShare);

        updateFolderShareService.update(folderId, targetId, dto, ownerId);

        Mockito
                .verify(verifyFolderOwnerUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getShareInvitation(folderId, targetId);

        Mockito
                .verify(findFolderRoleUseCase, Mockito.times(1))
                .execute(Mockito.any(FolderRoleCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .save(Mockito.any(FolderShareEntity.class));
    }

    @Test
    @DisplayName("폴더 권한 변경 - 실패 (자신에게 권한 변경 요청)")
    void updateFolderShareFailToSelf() {
        Long folderId = 1L;
        Long ownerId = 1L;
        Long targetId = 1L; // 자신에게 권한 변경 요청
        RequestInviteDto dto = new RequestInviteDto(FolderRole.EDITOR.name());

        Assertions
                .assertThatThrownBy(() -> updateFolderShareService.update(folderId, targetId, dto, ownerId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVITE_BAD_REQUEST);

        Mockito
                .verify(verifyFolderOwnerUseCase, Mockito.times(0))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(0))
                .getShareInvitation(folderId, targetId);

        Mockito
                .verify(findFolderRoleUseCase, Mockito.times(0))
                .execute(Mockito.any(FolderRoleCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(0))
                .save(Mockito.any(FolderShareEntity.class));
    }

    @Test
    @DisplayName("폴더 권한 변경 - 실패 (존재하지 않는 공유 요청)")
    void updateFolderShareFailNotFoundShare() {
        Long folderId = 1L;
        Long ownerId = 1L;
        Long targetId = 2L;
        RequestInviteDto dto = new RequestInviteDto(FolderRole.EDITOR.name());

        Mockito
                .doNothing()
                .when(verifyFolderOwnerUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(folderShareRepository.getShareInvitation(folderId, targetId))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> updateFolderShareService.update(folderId, targetId, dto, ownerId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_SHARE);

        Mockito
                .verify(verifyFolderOwnerUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getShareInvitation(folderId, targetId);

        Mockito
                .verify(findFolderRoleUseCase, Mockito.times(0))
                .execute(Mockito.any(FolderRoleCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(0))
                .save(Mockito.any(FolderShareEntity.class));
    }

    @Test
    @DisplayName("폴더 권한 변경 - 실패 (존재하지 않는 폴더 역할)")
    void updateFolderShareFailNotFoundFolderRole() {
        Long folderId = 1L;
        Long ownerId = 1L;
        Long targetId = 2L;
        RequestInviteDto dto = new RequestInviteDto(FolderRole.EDITOR.name());

        FolderRoleEntity folderRole = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleName", FolderRole.EDITOR)
                .sample();

        FolderShareEntity folderShare = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder.folderId", folderId)
                .set("ownerUser.userId", ownerId)
                .set("targetUser.userId", targetId)
                .set("role", folderRole)
                .sample();

        Mockito
                .doNothing()
                .when(verifyFolderOwnerUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(folderShareRepository.getShareInvitation(folderId, targetId))
                .thenReturn(Optional.of(folderShare));

        Mockito
                .when(findFolderRoleUseCase.execute(Mockito.any(FolderRoleCommand.class)))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> updateFolderShareService.update(folderId, targetId, dto, ownerId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_FOLDER_ROLE);

        Mockito
                .verify(verifyFolderOwnerUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getShareInvitation(folderId, targetId);

        Mockito
                .verify(findFolderRoleUseCase, Mockito.times(1))
                .execute(Mockito.any(FolderRoleCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(0))
                .save(Mockito.any(FolderShareEntity.class));
    }
}
