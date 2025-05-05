package org.y2k2.globa.infrastructure.persistence.record;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.factory.folder.FolderFactory;
import org.y2k2.globa.factory.folderrole.FolderRoleFactory;
import org.y2k2.globa.factory.foldershare.FolderShareFactory;
import org.y2k2.globa.factory.record.RecordFactory;
import org.y2k2.globa.factory.user.UserFactory;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.record.RecordFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.repository.FolderRoleTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.record.repository.RecordRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@Slf4j
@Import({
        RecordRepositoryImpl.class,
        FolderRoleTestRepositoryImpl.class,
        UserFixture.class,
        RecordFixture.class,
        FolderFixture.class,
        FolderRoleFixture.class,
        FolderShareFixture.class,
        UserFactory.class,
        RecordFactory.class,
        FolderFactory.class,
        FolderRoleFactory.class,
        FolderShareFactory.class,
        FolderRoleFactory.class,
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class RecordRepositoryTest {
    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private FolderRoleFixture folderRoleFixture;
    @Autowired
    private RecordFixture recordFixture;
    @Autowired
    private FolderShareFixture folderShareFixture;

    private UserEntity user;
    private FolderEntity folder;
    private RecordEntity record;

    @BeforeEach
    public void setUp() {
        user = userFixture.create();
        folder = folderFixture
                .withUser(user)
                .withTitle("Test Folder")
                .create();
        FolderRoleEntity folderRole = folderRoleFixture
                .withRole(FolderRole.OWNER)
                .create();
        folderShareFixture
                .withFolder(folder)
                .withOwner(user)
                .withTarget(user)
                .withRole(folderRole)
                .create();
        record = recordFixture
                .withUser(user)
                .withFolder(folder)
                .create();
    }

    @Test
    @DisplayName("문서 생성 - 성공")
    void createRecord() {
        String title = "Test Record";
        String path = "/{folder_id}/new_record.ogg";
        String size = "1024";
        RecordEntity newRecord = new RecordEntity();

        newRecord.setTitle(title);
        newRecord.setFolder(folder);
        newRecord.setUser(user);
        newRecord.setPath(path);
        newRecord.setSize(size);

        RecordEntity savedRecord = recordRepository.save(newRecord);

        Assertions.assertThat(savedRecord)
                .satisfies(recordEntity -> {
                    Assertions.assertThat(recordEntity.getRecordId()).isNotNull();
                    Assertions.assertThat(recordEntity.getTitle()).isEqualTo(title);
                    Assertions.assertThat(recordEntity.getFolder()).isEqualTo(folder);
                    Assertions.assertThat(recordEntity.getUser()).isEqualTo(user);
                    Assertions.assertThat(recordEntity.getPath()).isEqualTo(path);
                    Assertions.assertThat(recordEntity.getSize()).isEqualTo(size);
                });
    }

    @Test
    @DisplayName("문서 삭제 - 성공")
    void deleteRecord() {
        recordRepository.delete(record);
        Assertions.assertThat(recordRepository.getRecord(record.getRecordId())).isEmpty();
    }

    @Test
    @DisplayName("자신 소유 문서 ID 조회 - 성공")
    void getAllRecordId() {
        Long userId = user.getUserId();
        RecordEntity myOwnRecord = recordFixture
                .withUser(user)
                .withFolder(folder)
                .create();
        RecordEntity savedMyOwnRecord = recordRepository.save(myOwnRecord);

        UserEntity otherUser = userFixture
                .withSnsId("456456456")
                .withName("Other User")
                .withCode("QWEQWE")
                .withFcmToken("otherUserFcmToken")
                .create();
        FolderEntity otherUserFolder = folderFixture
                .withUser(otherUser)
                .withTitle("Other User Folder")
                .create();
        RecordEntity otherUserRecord = recordFixture
                .withUser(otherUser)
                .withFolder(otherUserFolder)
                .create();
        FolderRoleEntity editor = folderRoleFixture
                .withRole(FolderRole.EDITOR)
                .create();
        folderShareFixture
                .withFolder(otherUserFolder)
                .withOwner(otherUser)
                .withTarget(user)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();
        RecordEntity savedOtherUserRecord = recordRepository.save(otherUserRecord);

        List<Long> recordIds = recordRepository.getAllRecordId(userId);

        log.info("Original Record ID = {}", record.getRecordId());
        log.info("Saved My Own Record ID = {}", savedMyOwnRecord.getRecordId());
        log.info("Saved Other User Record ID = {}", savedOtherUserRecord.getRecordId());
        log.info("Record IDs = {}", recordIds);

        Assertions.assertThat(recordIds).contains(
                record.getRecordId(),
                savedMyOwnRecord.getRecordId(),
                savedOtherUserRecord.getRecordId()
        );
    }

    @Test
    @DisplayName("문서 경로 조회 - 성공")
    void getAllPath() {
        RecordEntity myOwnRecord = recordFixture
                .withUser(user)
                .withFolder(folder)
                .withPath("/" + folder.getFolderId() + "/new_record.ogg")
                .create();
        RecordEntity savedMyOwnRecord = recordRepository.save(myOwnRecord);

        UserEntity otherUser = userFixture
                .withSnsId("789789789")
                .withName("Other User2")
                .withCode("GGGKKK")
                .withFcmToken("otherUser2FcmToken")
                .create();
        FolderRoleEntity editor = folderRoleFixture
                .withRole(FolderRole.EDITOR)
                .create();
        folderShareFixture
                .withFolder(folder)
                .withOwner(user)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();
        RecordEntity otherUserRecord = recordFixture
                .withUser(otherUser)
                .withFolder(folder)
                .withPath("/" + folder.getFolderId() + "/other_record.ogg")
                .create();

        RecordEntity savedOtherUserRecord = recordRepository.save(otherUserRecord);

        List<String> paths = recordRepository.getAllPath(folder.getFolderId());

        log.info("Original Record Path = {}", record.getPath());
        log.info("Saved My Own Record Path = {}", savedMyOwnRecord.getPath());
        log.info("Saved Other User Record Path = {}", savedOtherUserRecord.getPath());
        log.info("Paths = {}", paths);

        Assertions.assertThat(paths).contains(
                record.getPath(),
                savedMyOwnRecord.getPath(),
                savedOtherUserRecord.getPath()
        );
    }

    @Test
    @DisplayName("문서 조회 - 성공")
    void getRecord() {
        UserEntity otherUser = userFixture
                .withSnsId("654654654")
                .withName("Other User3")
                .withCode("QWEzx2")
                .withFcmToken("otherUser3FcmToken")
                .create();
        FolderRoleEntity editor = folderRoleFixture
                .withRole(FolderRole.EDITOR)
                .create();
        folderShareFixture
                .withFolder(folder)
                .withOwner(user)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();
        RecordEntity otherUserRecord = recordFixture
                .withUser(otherUser)
                .withFolder(folder)
                .withPath("/" + folder.getFolderId() + "/other_record.ogg")
                .create();

        Pageable pageable = PageRequest.of(0, 10);
        Page<RecordEntity> response = recordRepository.getRecordsByFolderId(folder.getFolderId(), pageable);

        log.info("Original Record ID = {}", record.getRecordId());
        log.info("Saved Other User Record ID = {}", otherUserRecord.getRecordId());

        Assertions.assertThat(response.isEmpty()).isFalse();
        List<RecordEntity> records = response.getContent();
        Long total = response.getTotalElements();

        Assertions.assertThat(records).allSatisfy(recordEntity -> {
            log.info("Record ID = {}", recordEntity.getRecordId());

            Assertions.assertThat(recordEntity.getRecordId()).isIn(
                    record.getRecordId(),
                    otherUserRecord.getRecordId()
            );
        });

        Assertions.assertThat(total).isEqualTo(2);
    }
}
