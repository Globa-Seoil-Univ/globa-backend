package org.y2k2.globa.infrastructure.persistence.folder;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.factory.folder.FolderFactory;
import org.y2k2.globa.factory.folderrole.FolderRoleFactory;
import org.y2k2.globa.factory.foldershare.FolderShareFactory;
import org.y2k2.globa.factory.user.UserFactory;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folder.repository.FolderRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.folderrole.FolderRoleTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
@Import({
        FolderRepositoryImpl.class,
        UserFixture.class,
        FolderFixture.class,
        FolderRoleFixture.class,
        FolderShareFixture.class,
        UserFactory.class,
        FolderFactory.class,
        FolderRoleFactory.class,
        FolderShareFactory.class,
        FolderRoleFactory.class,
        FolderRoleTestRepositoryImpl.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class FolderRepositoryTest {
    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private FolderRoleFixture folderRoleFixture;
    @Autowired
    private FolderShareFixture folderShareFixture;

    private UserEntity user;
    private FolderEntity folder;
    private FolderRoleEntity folderRole;
    private FolderShareEntity folderShare;

    @BeforeEach
    void setUp() {
        user = userFixture.create();
        folder = folderFixture.withUser(user).create();
        folderRole = folderRoleFixture.create();
        folderShare = folderShareFixture
                .withOwner(user)
                .withTarget(user)
                .withFolder(folder)
                .withRole(folderRole)
                .create();
    }

    @Test
    @DisplayName("폴더 생성 - 성공")
    void createFolder() {
        UserEntity newUser = userFixture
                .withCode("QWERTY")
                .withFcmToken("FCM_TOKEN2")
                .withSnsId("SNS_ID2")
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
}
