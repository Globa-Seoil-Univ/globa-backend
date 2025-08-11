package org.y2k2.globa.application.folder.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.common.usecase.DeleteFilesUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class DeleteFolderServiceTest {
    @InjectMocks
    private DeleteFolderService deleteFolderService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private DeleteFilesUseCase deleteFilesUseCase;
    @Mock
    FolderShareRepository folderShareRepository;
    @Mock
    FolderRepository folderRepository;

    @Test
    @DisplayName("폴더 삭제 - 성공")
    void folderDeleteTest() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();
        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .sample();

        Mockito.when(folderShareRepository.isOwner(user.getUserId(), folder.getFolderId()))
                .thenReturn(true);
        Mockito.when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);
        Mockito.when(folderRepository.getFolderWithoutDefaultFolder(folder.getFolderId(), user))
                .thenReturn(Optional.of(folder));
        Mockito.doNothing()
                .when(folderRepository)
                .delete(ArgumentMatchers.any(FolderEntity.class));
        Mockito.doNothing()
                .when(deleteFilesUseCase)
                .execute(ArgumentMatchers.any(FolderEntity.class));

        deleteFolderService.delete(folder.getFolderId(), user.getUserId());

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .isOwner(user.getUserId(), folder.getFolderId());
        Mockito.verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());
        Mockito.verify(folderRepository, Mockito.times(1))
                .getFolderWithoutDefaultFolder(folder.getFolderId(), user);
        Mockito.verify(folderRepository, Mockito.times(1))
                .delete(ArgumentMatchers.any(FolderEntity.class));
        Mockito.verify(deleteFilesUseCase, Mockito.times(1))
                .execute(ArgumentMatchers.any(FolderEntity.class));
    }

    @Test
    @DisplayName("폴더 삭제 - 실패 (폴더 소유자 불일치)")
    void folderDeleteMismatchOwnerTest() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();
        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .sample();

        Mockito.when(folderShareRepository.isOwner(user.getUserId(), folder.getFolderId()))
                .thenReturn(false);

        Assertions.assertThatThrownBy(() -> deleteFolderService.delete(folder.getFolderId(), user.getUserId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISMATCH_FOLDER_OWNER);

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .isOwner(user.getUserId(), folder.getFolderId());
        Mockito.verify(findUserUseCase, Mockito.never())
                .execute(user.getUserId());
        Mockito.verify(folderRepository, Mockito.never())
                .getFolderWithoutDefaultFolder(folder.getFolderId(), user);
        Mockito.verify(folderRepository, Mockito.never())
                .delete(ArgumentMatchers.any(FolderEntity.class));
        Mockito.verify(deleteFilesUseCase, Mockito.never())
                .execute(ArgumentMatchers.any(FolderEntity.class));
    }

    @Test
    @DisplayName("폴더 삭제 - 실패 (존재하지 않는 폴더)")
    void folderDeleteNotExistTest() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();
        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .sample();

        Mockito.when(folderShareRepository.isOwner(user.getUserId(), folder.getFolderId()))
                .thenReturn(true);
        Mockito.when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);
        Mockito.when(folderRepository.getFolderWithoutDefaultFolder(folder.getFolderId(), user))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> deleteFolderService.delete(folder.getFolderId(), user.getUserId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_FOLDER);

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .isOwner(user.getUserId(), folder.getFolderId());
        Mockito.verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());
        Mockito.verify(folderRepository, Mockito.times(1))
                .getFolderWithoutDefaultFolder(folder.getFolderId(), user);
        Mockito.verify(folderRepository, Mockito.never())
                .delete(ArgumentMatchers.any(FolderEntity.class));
        Mockito.verify(deleteFilesUseCase, Mockito.never())
                .execute(ArgumentMatchers.any(FolderEntity.class));
    }
}
