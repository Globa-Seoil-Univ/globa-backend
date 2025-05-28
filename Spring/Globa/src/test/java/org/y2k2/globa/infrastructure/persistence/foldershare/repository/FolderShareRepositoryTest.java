package org.y2k2.globa.infrastructure.persistence.foldershare.repository;

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
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
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
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.repository.FolderRoleTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
@Import({
        FolderShareRepositoryImpl.class,
        FolderRoleTestRepositoryImpl.class,
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
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class FolderShareRepositoryTest {
    @Autowired
    private FolderShareRepository folderShareRepository;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private FolderRoleFixture folderRoleFixture;
    @Autowired
    private FolderShareFixture folderShareFixture;

    private UserEntity myUser;
    private FolderEntity myFolder;
    private FolderRoleEntity owner;
    private FolderRoleEntity editor;
    private FolderRoleEntity reader;

    @BeforeEach
    void setUp() {
        myUser = userFixture.create();
        myFolder = folderFixture
                .withUser(myUser)
                .create();
        owner = folderRoleFixture
                .withRole(FolderRole.OWNER)
                .create();
        editor = folderRoleFixture
                .withRole(FolderRole.EDITOR)
                .create();
        reader = folderRoleFixture
                .withRole(FolderRole.READER)
                .create();
    }

    @Test
    @DisplayName("폴더 공유 생성 - 성공")
    void createFolderShare() {
        FolderShareEntity folderShare = folderShareFixture
                .withFolder(myFolder)
                .withOwner(myUser)
                .withTarget(myUser)
                .withRole(owner)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();
        FolderShareEntity savedFolderShare = folderShareRepository.save(folderShare);

        log.info("Saved Folder Share = {}", savedFolderShare.getShareId());

        Assertions.assertThat(savedFolderShare.getShareId()).isNotNull();
        Assertions.assertThat(savedFolderShare.getFolder()).isEqualTo(myFolder);
        Assertions.assertThat(savedFolderShare.getTargetUser()).isEqualTo(myUser);
        Assertions.assertThat(savedFolderShare.getRole()).isEqualTo(owner);
        Assertions.assertThat(savedFolderShare.getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);
    }
}
