package org.y2k2.globa.application.user.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.folder.usecase.CreateDefaultFolderUseCase;
import org.y2k2.globa.application.folderrole.command.FolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.FindFolderRoleUseCase;
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GetUserServiceTest {
    @InjectMocks
    private GetUserService getUserService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private FindFolderRoleUseCase findFolderRoleUseCase;
    @Mock
    private CreateDefaultFolderUseCase createDefaultFolderUseCase;
    @Mock
    private FolderRepository folderRepository;

    @Test
    @DisplayName("유저 조회 - 성공")
    void getUser() {
        Long userId = 1L;

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .sample();

        FolderRoleEntity folderRole = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleName", FolderRole.OWNER)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .sample();

        Mockito.when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito.when(findFolderRoleUseCase.execute(ArgumentMatchers.any(FolderRoleCommand.class)))
                .thenReturn(Optional.of(folderRole));

        Mockito.when(folderRepository.getDefaultFolder(userId))
                .thenReturn(Optional.of(folder));

        ResponseUserDto response = getUserService.getUser(userId);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.userId()).isEqualTo(userId);
        Assertions.assertThat(response.publicFolderId()).isNotNull();
    }

    @Test
    @DisplayName("유저 조회 - 성공 (기본 폴더 없음)")
    void getUserWithoutDefaultFolder() {
        Long userId = 1L;

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .sample();

        FolderRoleEntity folderRole = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleName", FolderRole.OWNER)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .sample();

        Mockito.when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito.when(folderRepository.getDefaultFolder(userId))
                .thenReturn(Optional.empty());

        Mockito.when(findFolderRoleUseCase.execute(ArgumentMatchers.any(FolderRoleCommand.class)))
                .thenReturn(Optional.of(folderRole));

        Mockito.when(createDefaultFolderUseCase.execute(Mockito.any()))
                .thenReturn(folder);

        ResponseUserDto response = getUserService.getUser(userId);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.userId()).isEqualTo(userId);
        Assertions.assertThat(response.publicFolderId()).isNotNull();
    }

    @Test
    @DisplayName("유저 조회 - 실패 (유저 없음)")
    void getUserFailNotFound() {
        Long userId = 1L;

        Mockito.when(findUserUseCase.execute(userId))
                .thenThrow(new CustomException(ErrorCode.NOT_FOUND_USER));

        Assertions.assertThatThrownBy(() -> getUserService.getUser(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_USER);
    }

    @Test
    @DisplayName("유저 조회 - 실패 (삭제된 유저)")
    void getUserFailDeleted() {
        Long userId = 1L;

        Mockito.when(findUserUseCase.execute(userId))
                        .thenThrow(new CustomException(ErrorCode.DELETED_USER));

        Assertions.assertThatThrownBy(() -> getUserService.getUser(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DELETED_USER);
    }
}
