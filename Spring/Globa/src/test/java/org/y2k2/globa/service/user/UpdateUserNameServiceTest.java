package org.y2k2.globa.service.user;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.folder.command.CreateDefaultFolderCommand;
import org.y2k2.globa.application.folder.command.UpdateFolderNameCommand;
import org.y2k2.globa.application.folder.usecase.CreateDefaultFolderUseCase;
import org.y2k2.globa.application.folder.usecase.UpdateFolderNameUseCase;
import org.y2k2.globa.application.folderrole.command.GetFolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.GetFolderRoleUseCase;
import org.y2k2.globa.application.user.command.UpdateUserCommand;
import org.y2k2.globa.application.user.dto.request.RequestNameDto;
import org.y2k2.globa.application.user.service.UpdateUserNameService;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.user.usecase.UpdateUserUseCase;
import org.y2k2.globa.common.type.FolderRole;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UpdateUserNameServiceTest {
    @InjectMocks
    private UpdateUserNameService updateUserNameService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private GetFolderRoleUseCase getFolderRoleUseCase;
    @Mock
    private UpdateUserUseCase updateUserUseCase;
    @Mock
    private UpdateFolderNameUseCase updateFolderNameUseCase;
    @Mock
    private CreateDefaultFolderUseCase createDefaultFolderUseCase;
    @Mock
    private FolderRepository folderRepository;

    @Test
    @DisplayName("유저 이름 변경 - 성공")
    void updateUserName() {
        RequestNameDto dto = new RequestNameDto("New Name");

        UserEntity user = FixtureMonkey.builder()
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("name", "Old Name")
                .set("isDeleted", false)
                .sample();

        FolderRoleEntity folderRole = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleName", FolderRole.OWNER.getRoleName())
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user", user)
                .set("title", "Folder title")
                .sample();

        Mockito.when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito.doNothing()
                .when(updateUserUseCase)
                .execute(Mockito.any(UpdateUserCommand.class));

        Mockito.when(folderRepository.getDefaultFolder(user.getUserId()))
                .thenReturn(Optional.of(folder));

        Mockito.when(getFolderRoleUseCase.execute(Mockito.any(GetFolderRoleCommand.class)))
                .thenReturn(folderRole);

        Mockito.doNothing()
                .when(updateFolderNameUseCase)
                .execute(Mockito.any(UpdateFolderNameCommand.class));

        updateUserNameService.update(
                dto,
                user.getUserId()
        );

        Mockito.verify(updateUserUseCase, Mockito.times(1))
                .execute(Mockito.any(UpdateUserCommand.class));

        Mockito.verify(updateFolderNameUseCase, Mockito.times(1))
                .execute(Mockito.any(UpdateFolderNameCommand.class));

        Mockito.verify(folderRepository, Mockito.times(1))
                .getDefaultFolder(user.getUserId());

        Mockito.verify(createDefaultFolderUseCase, Mockito.times(0))
                .execute(Mockito.any(CreateDefaultFolderCommand.class));
    }

    @Test
    @DisplayName("유저 이름 변경 - 기본 폴더가 없는 경우")
    void updateUserNameWithoutDefaultFolder() {
        RequestNameDto dto = new RequestNameDto("New Name");

        UserEntity user = FixtureMonkey.builder()
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("name", "Old Name")
                .set("isDeleted", false)
                .sample();

        FolderRoleEntity folderRole = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleName", FolderRole.OWNER.getRoleName())
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user", user)
                .set("title", "Folder title")
                .sample();

        Mockito.when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito.doNothing()
                .when(updateUserUseCase)
                .execute(Mockito.any(UpdateUserCommand.class));

        Mockito.when(getFolderRoleUseCase.execute(Mockito.any(GetFolderRoleCommand.class)))
                .thenReturn(folderRole);

        Mockito.when(createDefaultFolderUseCase.execute(Mockito.any(CreateDefaultFolderCommand.class)))
                .thenReturn(folder);

        Mockito.when(folderRepository.getDefaultFolder(user.getUserId()))
                .thenReturn(Optional.empty());

        Mockito.doNothing()
                .when(updateFolderNameUseCase)
                .execute(Mockito.any(UpdateFolderNameCommand.class));

        updateUserNameService.update(
                dto,
                user.getUserId()
        );

        Mockito.verify(updateUserUseCase, Mockito.times(1))
                .execute(Mockito.any(UpdateUserCommand.class));

        Mockito.verify(updateFolderNameUseCase, Mockito.times(1))
                .execute(Mockito.any(UpdateFolderNameCommand.class));

        Mockito.verify(folderRepository, Mockito.times(1))
                .getDefaultFolder(user.getUserId());

        Mockito.verify(createDefaultFolderUseCase, Mockito.times(1))
                .execute(Mockito.any(CreateDefaultFolderCommand.class));
    }
}
