package org.y2k2.globa.infrastructure.persistence.folder.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.config.RepositoryIntegrationTest;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
@RepositoryIntegrationTest
public class FolderRepositoryTest {
    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private FolderRoleFixture folderRoleFixture;

    private UserEntity user;
    private FolderEntity folder;

    @BeforeEach
    void setUp() {
        user = userFixture.create();
        folder = folderFixture.withUser(user).create();
        folderRoleFixture.create();
    }

    @Test
    @DisplayName("폴더 생성 - 성공")
    void createFolder() {
        UserEntity newUser = userFixture
                .create();
        FolderEntity newFolder = folderFixture.withUser(newUser).create();
        FolderEntity savedFolder = folderRepository.save(newFolder);

        Assertions.assertThat(savedFolder.getFolderId()).isNotNull();
        Assertions.assertThat(savedFolder.getTitle()).isEqualTo(newFolder.getTitle());
        Assertions.assertThat(savedFolder.getUser()).isEqualTo(newUser);
    }

    @Test
    @DisplayName("폴더 삭제 - 성공")
    void deleteFolder() {
        folderRepository.delete(folder);
        Assertions.assertThat(folderRepository.getFolder(folder.getFolderId())).isEmpty();
    }

    @Test
    @DisplayName("폴더 조회 - 성공")
    void getFolder() {
        FolderEntity finedFolder = folderRepository.getFolder(folder.getFolderId()).orElseThrow();

        Assertions.assertThat(finedFolder.getFolderId()).isEqualTo(folder.getFolderId());
        Assertions.assertThat(finedFolder.getTitle()).isEqualTo(folder.getTitle());
        Assertions.assertThat(finedFolder.getUser().getUserId()).isEqualTo(folder.getUser().getUserId());
    }

    @Test
    @DisplayName("폴더 조회 - 실패 (존재하지 않는 폴더 ID)")
    void getFolderNotFound() {
        Long nonExistentFolderId = 999L;
        Assertions.assertThat(folderRepository.getFolder(nonExistentFolderId)).isEmpty();
    }

    @Test
    @DisplayName("기본 폴더 조회 - 성공")
    void getDefaultFolder() {
        FolderEntity defaultFolder = folderRepository.getDefaultFolder(user.getUserId()).orElseThrow();

        Assertions.assertThat(defaultFolder.getFolderId()).isEqualTo(folder.getFolderId());
        Assertions.assertThat(defaultFolder.getTitle()).isEqualTo(folder.getTitle());
        Assertions.assertThat(defaultFolder.getUser().getUserId()).isEqualTo(folder.getUser().getUserId());
    }

    @Test
    @DisplayName("기본 폴더 조회 - 실패 (존재하지 않는 사용자 ID)")
    void getDefaultFolderNotFound() {
        Long nonExistentUserId = 999L;
        Assertions.assertThat(folderRepository.getDefaultFolder(nonExistentUserId)).isEmpty();
    }

    @Test
    @DisplayName("기본 폴더를 제외한 폴더 조회 - 성공")
    void getFolderWithoutDefaultFolder() {
        FolderEntity newFolder = folderFixture.withUser(user).create();
        FolderEntity finedFolder = folderRepository.getFolderWithoutDefaultFolder(newFolder.getFolderId(), user).orElseThrow();

        Assertions.assertThat(finedFolder.getFolderId()).isEqualTo(newFolder.getFolderId());
        Assertions.assertThat(finedFolder.getTitle()).isEqualTo(newFolder.getTitle());
        Assertions.assertThat(finedFolder.getUser().getUserId()).isEqualTo(newFolder.getUser().getUserId());
    }

    @Test
    @DisplayName("기본 폴더를 제외한 폴더 조회 - 실패 (존재하지 않는 폴더 ID)")
    void getFolderWithoutDefaultFolderNotFound() {
        Long nonExistentFolderId = 999L;
        Assertions.assertThat(folderRepository.getFolderWithoutDefaultFolder(nonExistentFolderId, user)).isEmpty();
    }
}
