package org.y2k2.globa.application.folder.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.folder.command.UpdateFolderNameCommand;
import org.y2k2.globa.application.folder.usecase.UpdateFolderNameUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UpdateFolderNameServiceTest {
    @InjectMocks
    private UpdateFolderNameService updateFolderNameService;

    @Mock
    private FolderRepository folderRepository;
    @Mock
    private FolderShareRepository folderShareRepository;
    @Mock
    private UpdateFolderNameUseCase updateFolderNameUseCase;

    @Test
    @DisplayName("폴더 이름 수정 - 성공")
    void updateFolderName() {
        Long folderId = 1L;
        Long userId = 1L;
        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", folderId)
                .sample();

        Mockito.when(folderShareRepository.isOwner(userId, folderId))
                .thenReturn(true);
        Mockito.when(folderRepository.getFolder(folder.getFolderId()))
                .thenReturn(Optional.of(folder));
        Mockito.doNothing().when(updateFolderNameUseCase)
                .execute(ArgumentMatchers.any(UpdateFolderNameCommand.class));

        updateFolderNameService.update(folderId, "New Folder Name", userId);

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .isOwner(userId, folderId);
        Mockito.verify(folderRepository, Mockito.times(1))
                .getFolder(folder.getFolderId());
        Mockito.verify(updateFolderNameUseCase, Mockito.times(1))
                .execute(ArgumentMatchers.any(UpdateFolderNameCommand.class));
    }

    @Test
    @DisplayName("폴더 이름 수정 - 실패 (소유자 X)")
    void updateFolderNameFailNotOwner() {
        Long folderId = 1L;
        Long userId = 1L;
        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", folderId)
                .sample();

        Mockito.when(folderShareRepository.isOwner(userId, folderId))
                .thenReturn(false);

        Assertions.assertThatThrownBy(() -> updateFolderNameService.update(folderId, "New Folder Name", userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISMATCH_FOLDER_OWNER);

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .isOwner(userId, folderId);
        Mockito.verify(folderRepository, Mockito.times(0))
                .getFolder(folder.getFolderId());
        Mockito.verify(updateFolderNameUseCase, Mockito.times(0))
                .execute(ArgumentMatchers.any(UpdateFolderNameCommand.class));
    }

    @Test
    @DisplayName("폴더 이름 수정 - 실패 (폴더 X)")
    void updateFolderNameFailNotFound() {
        Long folderId = 1L;
        Long userId = 1L;

        Mockito.when(folderShareRepository.isOwner(userId, folderId))
                .thenReturn(true);
        Mockito.when(folderRepository.getFolder(folderId))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> updateFolderNameService.update(folderId, "New Folder Name", userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_FOLDER);

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .isOwner(userId, folderId);
        Mockito.verify(folderRepository, Mockito.times(1))
                .getFolder(folderId);
        Mockito.verify(updateFolderNameUseCase, Mockito.times(0))
                .execute(ArgumentMatchers.any(UpdateFolderNameCommand.class));
    }
}
