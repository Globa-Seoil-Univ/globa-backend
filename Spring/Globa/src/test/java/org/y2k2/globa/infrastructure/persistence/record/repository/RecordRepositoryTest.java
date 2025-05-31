package org.y2k2.globa.infrastructure.persistence.record.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.record.RecordFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.config.RepositoryIntegrationTest;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.record.projection.RecordSearchProjection;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@Slf4j
@RepositoryIntegrationTest
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
    private UserEntity otherUser;
    private FolderEntity folder;
    private FolderEntity otherFolder;
    private RecordEntity record;
    private FolderRoleEntity owner;
    private FolderRoleEntity editor;
    private FolderRoleEntity reader;

    @BeforeEach
    public void setUp() {
        user = userFixture.save(
                UserFixture
                        .builder()
                        .build()
        );
        otherUser = userFixture.save(
                UserFixture
                        .builder()
                        .name("Other User")
                        .build()
        );
        folder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .title("Test Folder")
                        .build()
        );
        otherFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(otherUser)
                        .title("Other User Folder")
                        .build()
        );

        owner = folderRoleFixture.save(
                FolderRoleFixture
                        .builder()
                        .role(FolderRole.OWNER)
                        .build()
        );
        editor = folderRoleFixture.save(
                FolderRoleFixture
                        .builder()
                        .role(FolderRole.EDITOR)
                        .build()
        );
        reader = folderRoleFixture.save(
                FolderRoleFixture
                        .builder()
                        .role(FolderRole.READER)
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .folder(folder)
                        .owner(user)
                        .target(user)
                        .role(owner)
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .folder(otherFolder)
                        .owner(otherUser)
                        .target(otherUser)
                        .role(owner)
                        .build()
        );

        record = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(folder)
                        .title("Test title record")
                        .path("/" + folder.getFolderId() + "/test_record.ogg")
                        .build()
        );
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
    @DisplayName("자신 소유 문서 ID 조회 - 성공 (외부 폴더 포함)")
    void getAllRecordId() {
        Long userId = user.getUserId();
        RecordEntity myOwnRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(folder)
                        .title("test title record2")
                        .build()
        );
        RecordEntity otherUserRecord = createRecordAndShare(otherFolder, otherUser, user, editor, InvitationStatus.ACCEPT);

        List<Long> recordIds = recordRepository.getAllRecordId(userId);

        log.info("Original Record ID = {}", record.getRecordId());
        log.info("Saved My Own Record ID = {}", myOwnRecord.getRecordId());
        log.info("Saved Other User Record ID = {}", otherUserRecord.getRecordId());
        log.info("Record IDs = {}", recordIds);

        Assertions.assertThat(recordIds).contains(
                record.getRecordId(),
                myOwnRecord.getRecordId(),
                otherUserRecord.getRecordId()
        );
    }

    @Test
    @DisplayName("문서 경로 조회 - 성공 (공유 사용자 포함)")
    void getAllPath() {
        RecordEntity myOwnRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(folder)
                        .title("test title record2")
                        .path("/" + folder.getFolderId() + "/my_own_record.ogg")
                        .build()
        );
        RecordEntity otherUserRecord = createRecordAndShare(folder, otherUser, user, editor, InvitationStatus.ACCEPT);

        List<String> paths = recordRepository.getAllPath(folder.getFolderId());

        log.info("Original Record Path = {}", record.getPath());
        log.info("Saved My Own Record Path = {}", myOwnRecord.getPath());
        log.info("Saved Other User Record Path = {}", otherUserRecord.getPath());
        log.info("Paths = {}", paths);

        Assertions.assertThat(paths).contains(
                record.getPath(),
                myOwnRecord.getPath(),
                otherUserRecord.getPath()
        );
    }

    @Test
    @DisplayName("특정 폴더 내 문서 조회 - 성공")
    void getRecord() {
        RecordEntity otherUserRecord = createRecordAndShare(folder, otherUser, user, editor, InvitationStatus.ACCEPT);

        Pageable pageable = PageRequest.of(0, 10);
        Page<RecordEntity> response = recordRepository.getRecordsByFolderId(folder.getFolderId(), pageable);

        log.info("Original Record ID = {}, Folder ID = {}", record.getRecordId(), folder.getFolderId());
        log.info("Other User Record ID = {}, Folder ID = {}", otherUserRecord.getRecordId(), otherFolder.getFolderId());

        Assertions.assertThat(response.isEmpty()).isFalse();
        List<RecordEntity> records = response.getContent();
        Long total = response.getTotalElements();

        Assertions.assertThat(records).allSatisfy(recordEntity -> {
            log.info("Record ID = {}, Folder ID = {}", recordEntity.getRecordId(), recordEntity.getFolder().getFolderId());

            Assertions.assertThat(recordEntity.getRecordId()).isIn(
                    record.getRecordId(),
                    otherUserRecord.getRecordId()
            );
        });

        Assertions.assertThat(total).isEqualTo(2);
    }

    @Test
    @DisplayName("접근 가능한 문서 조회 - 성공 (외부 폴더 포함)")
    void getAccessibleRecord() {
        RecordEntity otherUserRecord = createRecordAndShare(otherFolder, otherUser, user, reader, InvitationStatus.ACCEPT);

        Pageable pageable = PageRequest.of(0, 10);
        Page<RecordEntity> response = recordRepository.getAccessibleRecord(user.getUserId(), pageable);

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

    @Test
    @DisplayName("접근 가능한 문서 조회 - 성공 (미 수락 초대 제외)")
    void getAccessibleWithoutInvitation() {
        RecordEntity otherUserRecord = createRecordAndShare(otherFolder, otherUser, user, reader, InvitationStatus.PENDING);

        Pageable pageable = PageRequest.of(0, 10);
        Page<RecordEntity> response = recordRepository.getAccessibleRecord(user.getUserId(), pageable);

        log.info("Original Record ID = {}", record.getRecordId());
        log.info("Saved Other User Record ID = {}", otherUserRecord.getRecordId());

        Assertions.assertThat(response.isEmpty()).isFalse();
        List<RecordEntity> records = response.getContent();
        Long total = response.getTotalElements();

        Assertions.assertThat(records).allSatisfy(recordEntity -> {
            log.info("Record ID = {}", recordEntity.getRecordId());
            Assertions.assertThat(recordEntity.getRecordId()).isEqualTo(record.getRecordId());
        });
        Assertions.assertThat(total).isEqualTo(1);
    }

    @Test
    @DisplayName("문서 검색 - 성공 (외부 폴더 포함)")
    void getRecordByKeyword() {
        RecordEntity otherUserRecord = createRecordAndShare(otherFolder, otherUser, user, editor, InvitationStatus.ACCEPT);

        String keyword = "title";
        Pageable pageable = PageRequest.of(0, 10);

        Page<RecordSearchProjection> response = recordRepository.getRecordByKeyword(user.getUserId(), keyword, pageable);

        log.info("Original Record ID = {}, Title = {}", record.getRecordId(), record.getTitle());
        log.info("Other User Record ID = {}, Title = {}", otherUserRecord.getRecordId(), otherUserRecord.getTitle());
        log.info("Keyword = {}", keyword);

        Assertions.assertThat(response.isEmpty()).isFalse();

        List<RecordSearchProjection> records = response.getContent();
        Long total = response.getTotalElements();

        Assertions.assertThat(records).allSatisfy(recordEntity -> {
            log.info("Record ID = {}, Title = {}", recordEntity.getRecordId(), recordEntity.getTitle());
            Assertions.assertThat(recordEntity.getRecordId()).isIn(
                    record.getRecordId(),
                    otherUserRecord.getRecordId()
            );
            Assertions.assertThat(recordEntity.getTitle()).contains(keyword);
        });

        Assertions.assertThat(total).isEqualTo(2);
    }

    @Test
    @DisplayName("문서 검색 - 성공 (미 수락 초대 제외)")
    void getRecordByKeywordWithoutInvitation() {
        RecordEntity otherUserRecord = createRecordAndShare(otherFolder, otherUser, user, editor, InvitationStatus.PENDING);

        String keyword = "title";
        Pageable pageable = PageRequest.of(0, 10);

        Page<RecordSearchProjection> response = recordRepository.getRecordByKeyword(user.getUserId(), keyword, pageable);

        log.info("Original Record ID = {}, Title = {}", record.getRecordId(), record.getTitle());
        log.info("Other User Record ID = {}, Title = {}", otherUserRecord.getRecordId(), otherUserRecord.getTitle());
        log.info("Keyword = {}", keyword);

        Assertions.assertThat(response.isEmpty()).isFalse();

        List<RecordSearchProjection> records = response.getContent();
        Long total = response.getTotalElements();

        Assertions.assertThat(records).allSatisfy(recordEntity -> {
            log.info("Record ID = {}, Title = {}", recordEntity.getRecordId(), recordEntity.getTitle());
            Assertions.assertThat(recordEntity.getRecordId()).isEqualTo(record.getRecordId());
            Assertions.assertThat(recordEntity.getTitle()).contains(keyword);
        });

        Assertions.assertThat(total).isEqualTo(1);
    }

    @Test
    @DisplayName("공유 받은 문서 조회 - 성공")
    void getReceivingRecord() {
        RecordEntity otherUserRecord = createRecordAndShare(otherFolder, otherUser, user, reader, InvitationStatus.ACCEPT);

        Pageable pageable = PageRequest.of(0, 10);
        Page<RecordEntity> response = recordRepository.getInvitedRecord(user.getUserId(), pageable);

        log.info("Original Record ID = {}, Title = {}", record.getRecordId(), record.getTitle());
        log.info("Other User Record ID = {}, Title = {}", otherUserRecord.getRecordId(), otherUserRecord.getTitle());

        Assertions.assertThat(response.isEmpty()).isFalse();
        List<RecordEntity> records = response.getContent();
        Long total = response.getTotalElements();

        Assertions.assertThat(records).allSatisfy(recordEntity -> {
            log.info("Record ID = {}, Title = {}", recordEntity.getRecordId(), recordEntity.getTitle());
            Assertions.assertThat(recordEntity.getRecordId()).isEqualTo(otherUserRecord.getRecordId());
            Assertions.assertThat(recordEntity.getTitle()).isEqualTo(otherUserRecord.getTitle());
        });

        Assertions.assertThat(total).isEqualTo(1);
    }

    @Test
    @DisplayName("공유 하는 문서 조회 - 성공")
    void getSharingRecord() {
        RecordEntity twoRecord = createRecordAndShare(folder, user, otherUser, reader, InvitationStatus.ACCEPT);

        FolderEntity noShareFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .title("No Share Folder")
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .folder(noShareFolder)
                        .owner(user)
                        .target(user)
                        .role(owner)
                        .build()
        );

        RecordEntity noShareRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(noShareFolder)
                        .title("No Share Record")
                        .build()
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<RecordEntity> response = recordRepository.getOwnedRecord(user.getUserId(), pageable);

        log.info("Original Record ID = {}, Title = {}", record.getRecordId(), record.getTitle());
        log.info("Two Record ID = {}, Title = {}", twoRecord.getRecordId(), twoRecord.getTitle());
        log.info("No Share Record ID = {}, Title = {}", noShareFolder.getFolderId(), noShareFolder.getTitle());

        Assertions.assertThat(response.isEmpty()).isFalse();
        List<RecordEntity> records = response.getContent();
        Long total = response.getTotalElements();

        Assertions.assertThat(records).allSatisfy(recordEntity -> {
            log.info("Record ID = {}, Title = {}", recordEntity.getRecordId(), recordEntity.getTitle());
            Assertions.assertThat(recordEntity.getRecordId()).isIn(
                    record.getRecordId(),
                    twoRecord.getRecordId()
            );

            Assertions.assertThat(recordEntity.getRecordId()).isNotEqualTo(noShareRecord.getRecordId());
        });

        Assertions.assertThat(total).isEqualTo(2);
    }

    private RecordEntity createRecordAndShare(
            FolderEntity folder,
            UserEntity owner,
            UserEntity target,
            FolderRoleEntity role,
            InvitationStatus status
    ) {
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .folder(folder)
                        .owner(owner)
                        .target(target)
                        .role(role)
                        .status(status)
                        .build()
        );

        return recordFixture.save(
                RecordFixture
                        .builder()
                        .user(target)
                        .folder(folder)
                        .title("test title record2")
                        .path("/" + folder.getFolderId() + "/other_record.ogg")
                        .build()
        );
    }
}
