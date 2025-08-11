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
import org.springframework.context.ApplicationEventPublisher;
import org.y2k2.globa.application.folderrole.command.FolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.FindFolderRoleUseCase;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.dto.request.RequestInviteDto;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithInvitationDto;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class InviteFolderShareServiceTest {
    @InjectMocks
    private InviteFolderShareService inviteFolderShareService;

    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private FindFolderRoleUseCase findFolderRoleUseCase;
    @Mock
    private CreateNotificationUseCase createNotificationUseCase;
    @Mock
    private VerifyFolderOwnerUseCase verifyFolderOwnerUseCase;
    @Mock
    private FolderRepository folderRepository;
    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("공유 초대 - 성공")
    void inviteSuccess() {
        Long folderId = 1L;
        Long ownerId = 1L;
        Long targetId = 2L;

        RequestInviteDto dto = new RequestInviteDto(FolderRole.EDITOR.toString());
        UserEntity owner = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", ownerId)
                .set("isDeleted", false)
                .sample();
        UserEntity target = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", targetId)
                .set("isDeleted", false)
                .sample();
        FolderEntity folder = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", folderId)
                .set("user", owner)
                .sample();
        FolderRoleEntity editor = FixtureMonkey
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
                .set("folder", folder)
                .set("invitationStatus", InvitationStatus.PENDING)
                .set("role", editor)
                .set("ownerUser", owner)
                .set("targetUser", target)
                .sample();

        Mockito
                .when(findUserUseCase.execute(ownerId))
                .thenReturn(owner);

        Mockito
                .when(findUserUseCase.execute(targetId))
                .thenReturn(target);

        Mockito
                .when(folderRepository.getFolder(folderId))
                .thenReturn(Optional.of(folder));

        Mockito
                .doNothing()
                .when(verifyFolderOwnerUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(folderShareRepository.isInvited(targetId, folderId))
                .thenReturn(false);

        Mockito
                .when(findFolderRoleUseCase.execute(Mockito.any(FolderRoleCommand.class)))
                .thenReturn(Optional.of(editor));

        Mockito
                .when(folderShareRepository.save(Mockito.any(FolderShareEntity.class)))
                .thenReturn(folderShare);

        Mockito
                .doNothing()
                .when(createNotificationUseCase)
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.any(RequestNotificationWithInvitationDto.class));

        inviteFolderShareService
                .invite(folderId, targetId, dto, ownerId);

        Mockito
                .verify(findUserUseCase, Mockito.times(2))
                .execute(Mockito.anyLong());

        Mockito
                .verify(folderRepository, Mockito.times(1))
                .getFolder(Mockito.anyLong());

        Mockito
                .verify(verifyFolderOwnerUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .isInvited(Mockito.anyLong(), Mockito.anyLong());

        Mockito
                .verify(findFolderRoleUseCase, Mockito.times(1))
                .execute(Mockito.any(FolderRoleCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .save(Mockito.any(FolderShareEntity.class));

        Mockito
                .verify(createNotificationUseCase, Mockito.times(1))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.any(RequestNotificationWithInvitationDto.class));
    }

    @Test
    @DisplayName("공유 초대 - 실패 (폴더 존재 X)")
    void inviteFolderNotFound() {
        Long folderId = 1L;
        Long ownerId = 1L;
        Long targetId = 2L;

        RequestInviteDto dto = new RequestInviteDto(FolderRole.EDITOR.toString());
        UserEntity owner = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", ownerId)
                .set("isDeleted", false)
                .sample();
        UserEntity target = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", targetId)
                .set("isDeleted", false)
                .sample();
        FolderEntity folder = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", folderId)
                .set("user", owner)
                .sample();

        Mockito
                .when(findUserUseCase.execute(ownerId))
                .thenReturn(owner);

        Mockito
                .when(findUserUseCase.execute(targetId))
                .thenReturn(target);

        Mockito
                .when(folderRepository.getFolder(folderId))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> inviteFolderShareService.invite(folderId, targetId, dto, ownerId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_FOLDER);

        Mockito
                .verify(findUserUseCase, Mockito.times(2))
                .execute(Mockito.anyLong());

        Mockito
                .verify(folderRepository, Mockito.times(1))
                .getFolder(Mockito.anyLong());

        Mockito
                .verify(verifyFolderOwnerUseCase, Mockito.times(0))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(0))
                .isInvited(Mockito.anyLong(), Mockito.anyLong());

        Mockito
                .verify(findFolderRoleUseCase, Mockito.times(0))
                .execute(Mockito.any(FolderRoleCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(0))
                .save(Mockito.any(FolderShareEntity.class));

        Mockito
                .verify(createNotificationUseCase, Mockito.times(0))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(publisher, Mockito.times(0))
                .publishEvent(Mockito.any(RequestNotificationWithInvitationDto.class));
    }

    @Test
    @DisplayName("공유 초대 - 실패 (이미 초대된 사용자)")
    void inviteAlreadyInvited() {
        Long folderId = 1L;
        Long ownerId = 1L;
        Long targetId = 2L;

        RequestInviteDto dto = new RequestInviteDto(FolderRole.EDITOR.toString());
        UserEntity owner = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", ownerId)
                .set("isDeleted", false)
                .sample();
        UserEntity target = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", targetId)
                .set("isDeleted", false)
                .sample();
        FolderEntity folder = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", folderId)
                .set("user", owner)
                .sample();

        Mockito
                .when(findUserUseCase.execute(ownerId))
                .thenReturn(owner);

        Mockito
                .when(findUserUseCase.execute(targetId))
                .thenReturn(target);

        Mockito
                .when(folderRepository.getFolder(folderId))
                .thenReturn(Optional.of(folder));

        Mockito
                .doNothing()
                .when(verifyFolderOwnerUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(folderShareRepository.isInvited(targetId, folderId))
                .thenReturn(true);

        Assertions
                .assertThatThrownBy(() -> inviteFolderShareService.invite(folderId, targetId, dto, ownerId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SHARE_USER_DUPLICATED);

        Mockito
                .verify(findUserUseCase, Mockito.times(2))
                .execute(Mockito.anyLong());

        Mockito
                .verify(folderRepository, Mockito.times(1))
                .getFolder(Mockito.anyLong());

        Mockito
                .verify(verifyFolderOwnerUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .isInvited(Mockito.anyLong(), Mockito.anyLong());

        Mockito
                .verify(findFolderRoleUseCase, Mockito.times(0))
                .execute(Mockito.any(FolderRoleCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(0))
                .save(Mockito.any(FolderShareEntity.class));

        Mockito
                .verify(createNotificationUseCase, Mockito.times(0))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(publisher, Mockito.times(0))
                .publishEvent(Mockito.any(RequestNotificationWithInvitationDto.class));
    }

    @Test
    @DisplayName("공유 초대 - 실패 (폴더 역할 존재 X)")
    void inviteFolderRoleNotFound() {
        Long folderId = 1L;
        Long ownerId = 1L;
        Long targetId = 2L;

        RequestInviteDto dto = new RequestInviteDto(FolderRole.EDITOR.toString());
        UserEntity owner = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", ownerId)
                .set("isDeleted", false)
                .sample();
        UserEntity target = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", targetId)
                .set("isDeleted", false)
                .sample();
        FolderEntity folder = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", folderId)
                .set("user", owner)
                .sample();

        Mockito
                .when(findUserUseCase.execute(ownerId))
                .thenReturn(owner);

        Mockito
                .when(findUserUseCase.execute(targetId))
                .thenReturn(target);

        Mockito
                .when(folderRepository.getFolder(folderId))
                .thenReturn(Optional.of(folder));

        Mockito
                .doNothing()
                .when(verifyFolderOwnerUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(folderShareRepository.isInvited(targetId, folderId))
                .thenReturn(false);

        Mockito
                .when(findFolderRoleUseCase.execute(Mockito.any(FolderRoleCommand.class)))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> inviteFolderShareService.invite(folderId, targetId, dto, ownerId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_FOLDER_ROLE);

        Mockito
                .verify(findUserUseCase, Mockito.times(2))
                .execute(Mockito.anyLong());

        Mockito
                .verify(folderRepository, Mockito.times(1))
                .getFolder(Mockito.anyLong());

        Mockito
                .verify(verifyFolderOwnerUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .isInvited(Mockito.anyLong(), Mockito.anyLong());

        Mockito
                .verify(findFolderRoleUseCase, Mockito.times(1))
                .execute(Mockito.any(FolderRoleCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(0))
                .save(Mockito.any(FolderShareEntity.class));

        Mockito
                .verify(createNotificationUseCase, Mockito.times(0))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(publisher, Mockito.times(0))
                .publishEvent(Mockito.any(RequestNotificationWithInvitationDto.class));
    }
}
