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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import java.util.List;
import java.util.Optional;

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
    private UserEntity otherUser;
    private FolderEntity myDefaultFolder;
    private FolderRoleEntity owner;
    private FolderRoleEntity editor;
    private FolderRoleEntity reader;

    @BeforeEach
    void setUp() {
        myUser = userFixture.create();
        otherUser = userFixture
                .withName("Other User")
                .create();
        myDefaultFolder = folderFixture
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

        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(myUser)
                .withRole(owner)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();
    }

    @Test
    @DisplayName("폴더 공유 생성 - 성공")
    void createFolderShare() {
        FolderShareEntity folderShare = folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(myUser)
                .withRole(owner)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        log.info("Saved Folder Share = {}", folderShare.getShareId());

        Assertions.assertThat(folderShare.getShareId()).isNotNull();
        Assertions.assertThat(folderShare.getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(folderShare.getTargetUser()).isEqualTo(myUser);
        Assertions.assertThat(folderShare.getRole()).isEqualTo(owner);
        Assertions.assertThat(folderShare.getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);
    }

    @Test
    @DisplayName("여러 개 폴더 공유 생성 - 성공")
    void createMultipleFolderShares() {
        FolderShareEntity myShare = folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(myUser)
                .withRole(owner)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();
        FolderShareEntity otherShare = folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.PENDING)
                .create();

        List<FolderShareEntity> savedShares = folderShareRepository.saveAll(List.of(myShare, otherShare));

        log.info("Saved Folder Shares Ids = {}", savedShares.stream()
                .map(FolderShareEntity::getShareId)
                .toList());

        Assertions.assertThat(savedShares).hasSize(2);
        Assertions.assertThat(savedShares.get(0).getShareId()).isNotNull();
        Assertions.assertThat(savedShares.get(0).getRole()).isEqualTo(owner);
        Assertions.assertThat(savedShares.get(0).getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(savedShares.get(0).getTargetUser()).isEqualTo(myUser);
        Assertions.assertThat(savedShares.get(0).getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);

        Assertions.assertThat(savedShares.get(1).getShareId()).isNotNull();
        Assertions.assertThat(savedShares.get(1).getRole()).isEqualTo(editor);
        Assertions.assertThat(savedShares.get(1).getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(savedShares.get(1).getTargetUser()).isEqualTo(otherUser);
        Assertions.assertThat(savedShares.get(1).getInvitationStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    @DisplayName("폴더 공유 삭제 - 성공")
    void deleteFolderShare() {
        FolderShareEntity folderShare = folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        log.info("Deleting Folder Share Id = {}", folderShare.getShareId());

        folderShareRepository.delete(folderShare);
    }

    @Test
    @DisplayName("폴더 접근 가능 여부 확인 - 성공")
    void isAccessible() {
        FolderShareEntity folderShare = folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        Boolean isAccessible = folderShareRepository.isAccessible(otherUser.getUserId(), myDefaultFolder.getFolderId());

        log.info("Is Accessible: {}", isAccessible);
        Assertions.assertThat(isAccessible).isTrue();
    }

    @Test
    @DisplayName("폴더 접근 불가능 여부 확인 - 성공 (Pending 상태)")
    void isNotAccessibleByPending() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.PENDING)
                .create();

        Boolean isAccessible = folderShareRepository.isAccessible(otherUser.getUserId(), myDefaultFolder.getFolderId());

        log.info("Is Accessible: {}", isAccessible);
        Assertions.assertThat(isAccessible).isFalse();
    }

    @Test
    @DisplayName("폴더 접근 불가능 여부 확인 - 성공 (초대 없음)")
    void isNotAccessibleByNoInvitation() {
        Boolean isAccessible = folderShareRepository.isAccessible(otherUser.getUserId(), myDefaultFolder.getFolderId());

        log.info("Is Accessible: {}", isAccessible);
        Assertions.assertThat(isAccessible).isFalse();
    }

    @Test
    @DisplayName("폴더 소유자 여부 확인 - 성공")
    void isOwner() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(myUser)
                .withRole(owner)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        Boolean isMyOwner = folderShareRepository.isOwner(myUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is My Owner: {}", isMyOwner);

        Assertions.assertThat(isMyOwner).isTrue();
    }

    @Test
    @DisplayName("폴더 소유자 여부 확인 - 성공 (EDITOR 권한)")
    void isNotOwner() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        Boolean isMyOwner = folderShareRepository.isOwner(otherUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is My Owner: {}", isMyOwner);

        Assertions.assertThat(isMyOwner).isFalse();
    }

    @Test
    @DisplayName("폴더 소유자 여부 확인 - 성공 (READER 권한)")
    void isNotOwnerByReader() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(reader)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        Boolean isMyOwner = folderShareRepository.isOwner(otherUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is My Owner: {}", isMyOwner);

        Assertions.assertThat(isMyOwner).isFalse();
    }

    @Test
    @DisplayName("폴더 소유 여부 확인 - 성공 (초대 없음)")
    void isNotOwnerByNoInvitation() {
        Boolean isMyOwner = folderShareRepository.isOwner(otherUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is My Owner: {}", isMyOwner);

        Assertions.assertThat(isMyOwner).isFalse();
    }

    @Test
    @DisplayName("폴더 소유 여부 확인 - 성공 (Pending 상태)")
    void isNotOwnerByPending() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.PENDING)
                .create();

        Boolean isMyOwner = folderShareRepository.isOwner(otherUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is My Owner: {}", isMyOwner);

        Assertions.assertThat(isMyOwner).isFalse();
    }

    @Test
    @DisplayName("폴더 초대 여부 확인 - 성공")
    void isInvited() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        Boolean isInvited = folderShareRepository.isInvited(otherUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is Invited: {}", isInvited);

        Assertions.assertThat(isInvited).isTrue();
    }

    @Test
    @DisplayName("폴더 초대 여부 확인 - 성공 (Pending 상태)")
    void isInvitedByPending() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.PENDING)
                .create();

        Boolean isInvited = folderShareRepository.isInvited(otherUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is Invited: {}", isInvited);

        Assertions.assertThat(isInvited).isTrue();
    }

    @Test
    @DisplayName("폴더 초대 여부 확인 - 성공 (초대 없음)")
    void isNotInvited() {
        Boolean isInvited = folderShareRepository.isInvited(otherUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is Invited: {}", isInvited);

        Assertions.assertThat(isInvited).isFalse();
    }

    @Test
    @DisplayName("폴더 편집 권한 여부 확인 - 성공 (OWNER 권한)")
    void isWritable() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(myUser)
                .withRole(owner)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        Boolean isWritable = folderShareRepository.isWritable(myUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is Writable: {}", isWritable);

        Assertions.assertThat(isWritable).isTrue();
    }

    @Test
    @DisplayName("폴더 편집 권한 여부 확인 - 성공 (EDITOR 권한)")
    void isWritableByEditor() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        Boolean isWritable = folderShareRepository.isWritable(otherUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is Writable: {}", isWritable);

        Assertions.assertThat(isWritable).isTrue();
    }

    @Test
    @DisplayName("폴더 편집 권한 여부 확인 - 성공 (READER 권한)")
    void isNotWritableByReader() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(reader)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        Boolean isWritable = folderShareRepository.isWritable(otherUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is Writable: {}", isWritable);

        Assertions.assertThat(isWritable).isFalse();
    }

    @Test
    @DisplayName("폴더 편집 권한 여부 확인 - 성공 (Pending 상태)")
    void isNotWritableByPending() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.PENDING)
                .create();

        Boolean isWritable = folderShareRepository.isWritable(otherUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is Writable: {}", isWritable);

        Assertions.assertThat(isWritable).isFalse();
    }

    @Test
    @DisplayName("폴더 편집 권한 여부 확인 - 성공 (초대 없음)")
    void isNotWritableByNoInvitation() {
        Boolean isWritable = folderShareRepository.isWritable(otherUser.getUserId(), myDefaultFolder.getFolderId());
        log.info("Is Writable: {}", isWritable);

        Assertions.assertThat(isWritable).isFalse();
    }

    @Test
    @DisplayName("폴더 공유 초대 조회 - 성공")
    void getShareInvitations() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.PENDING)
                .create();

        Pageable pageable = PageRequest.of(0, 10);
        Page<FolderShareEntity> invitations = folderShareRepository.getShareInvitations(myDefaultFolder.getFolderId(), pageable);

        log.info("Share Invitations: {}", invitations.getContent());

        Assertions.assertThat(invitations.getContent()).hasSize(2);
        Assertions.assertThat(invitations.getContent().get(0).getTargetUser()).isEqualTo(myUser);
        Assertions.assertThat(invitations.getContent().get(0).getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(invitations.getContent().get(0).getRole()).isEqualTo(owner);
        Assertions.assertThat(invitations.getContent().get(0).getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);

        Assertions.assertThat(invitations.getContent().get(1).getTargetUser()).isEqualTo(otherUser);
        Assertions.assertThat(invitations.getContent().get(1).getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(invitations.getContent().get(1).getRole()).isEqualTo(editor);
        Assertions.assertThat(invitations.getContent().get(1).getInvitationStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    @DisplayName("기본 폴더를 제외한 초대 조회 - 성공")
    void getShareInvitationsWithoutDefault() {
        FolderEntity twiceMyFolder = folderFixture
                .withUser(myUser)
                .create();
        FolderShareEntity twiceMyShare = folderShareFixture
                .withFolder(twiceMyFolder)
                .withOwner(myUser)
                .withTarget(myUser)
                .withRole(owner)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        FolderEntity otherFolder = folderFixture
                .withUser(otherUser)
                .create();
        FolderShareEntity otherShare = folderShareFixture
                .withFolder(otherFolder)
                .withOwner(otherUser)
                .withTarget(myUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        folderShareRepository.saveAll(List.of(twiceMyShare, otherShare));

        Pageable pageable = PageRequest.of(0, 10);
        Page<FolderShareEntity> invitations = folderShareRepository.getInvitationsForFolderExcludingDefault(myUser.getUserId(), pageable);

        log.info("Invitations Excluding Default: {}", invitations.getContent());

        Assertions.assertThat(invitations.getContent()).hasSize(2);
        Assertions.assertThat(invitations.getContent().get(0).getFolder()).isNotEqualTo(myDefaultFolder);
        Assertions.assertThat(invitations.getContent().get(0).getFolder()).isEqualTo(twiceMyFolder);
        Assertions.assertThat(invitations.getContent().get(0).getTargetUser()).isEqualTo(myUser);
        Assertions.assertThat(invitations.getContent().get(0).getRole()).isEqualTo(owner);
        Assertions.assertThat(invitations.getContent().get(0).getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);

        Assertions.assertThat(invitations.getContent().get(1).getFolder()).isNotEqualTo(myDefaultFolder);
        Assertions.assertThat(invitations.getContent().get(1).getFolder()).isEqualTo(otherFolder);
        Assertions.assertThat(invitations.getContent().get(1).getTargetUser()).isEqualTo(myUser);
        Assertions.assertThat(invitations.getContent().get(1).getRole()).isEqualTo(editor);
        Assertions.assertThat(invitations.getContent().get(1).getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);
    }

    @Test
    @DisplayName("기본 폴더를 제외한 초대 조회 - 성공 (Pending 상태)")
    void getShareInvitationsWithoutDefaultByPending() {
        FolderEntity twiceMyFolder = folderFixture
                .withUser(myUser)
                .create();
        FolderShareEntity twiceMyShare = folderShareFixture
                .withFolder(twiceMyFolder)
                .withOwner(myUser)
                .withTarget(myUser)
                .withRole(owner)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        FolderEntity otherFolder = folderFixture
                .withUser(otherUser)
                .create();
        FolderShareEntity otherShare = folderShareFixture
                .withFolder(otherFolder)
                .withOwner(otherUser)
                .withTarget(myUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.PENDING)
                .create();

        folderShareRepository.saveAll(List.of(twiceMyShare, otherShare));

        Pageable pageable = PageRequest.of(0, 10);
        Page<FolderShareEntity> invitations = folderShareRepository.getInvitationsForFolderExcludingDefault(myUser.getUserId(), pageable);

        log.info("Invitations Excluding Default: {}", invitations.getContent());

        Assertions.assertThat(invitations.getContent()).hasSize(1);
        Assertions.assertThat(invitations.getContent().get(0).getFolder()).isNotEqualTo(myDefaultFolder);
        Assertions.assertThat(invitations.getContent().get(0).getFolder()).isEqualTo(twiceMyFolder);
        Assertions.assertThat(invitations.getContent().get(0).getTargetUser()).isEqualTo(myUser);
        Assertions.assertThat(invitations.getContent().get(0).getRole()).isEqualTo(owner);
        Assertions.assertThat(invitations.getContent().get(0).getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);
    }

    @Test
    @DisplayName("기본 폴더를 제외한 초대 조회 - 성공 (기본 폴더만 존재)")
    void getShareInvitationsWithoutDefaultOnlyDefault() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FolderShareEntity> invitations = folderShareRepository.getInvitationsForFolderExcludingDefault(myUser.getUserId(), pageable);

        log.info("Invitations Excluding Default: {}", invitations.getContent());

        Assertions.assertThat(invitations.getContent()).isEmpty();
    }

    @Test
    @DisplayName("특정 폴더 초대 조회 - 성공")
    void getAllShareInvitations() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.PENDING)
                .create();

        List<FolderShareEntity> allInvitations = folderShareRepository.getAllShareInvitations(myDefaultFolder.getFolderId());

        log.info("All Share Invitations: {}", allInvitations);

        Assertions.assertThat(allInvitations).hasSize(2);
        Assertions.assertThat(allInvitations.get(0).getTargetUser()).isEqualTo(myUser);
        Assertions.assertThat(allInvitations.get(0).getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(allInvitations.get(0).getRole()).isEqualTo(owner);
        Assertions.assertThat(allInvitations.get(0).getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);

        Assertions.assertThat(allInvitations.get(1).getTargetUser()).isEqualTo(otherUser);
        Assertions.assertThat(allInvitations.get(1).getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(allInvitations.get(1).getRole()).isEqualTo(editor);
        Assertions.assertThat(allInvitations.get(1).getInvitationStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    @DisplayName("자신을 제외한 특정 폴더 초대 조회 - 성공 (Pending 상태)")
    void getAllShareInvitationsExcludingSelfByPending() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.PENDING)
                .create();

        List<FolderShareEntity> allInvitations = folderShareRepository.getAllShareInvitationsWithoutMe(myDefaultFolder.getFolderId(), myUser.getUserId());

        log.info("All Share Invitations Excluding Self: {}", allInvitations);

        Assertions.assertThat(allInvitations).hasSize(1);
        Assertions.assertThat(allInvitations.get(0).getTargetUser()).isEqualTo(otherUser);
        Assertions.assertThat(allInvitations.get(0).getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(allInvitations.get(0).getRole()).isEqualTo(editor);
        Assertions.assertThat(allInvitations.get(0).getInvitationStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    @DisplayName("자신을 제외한 특정 폴더 초대 조회 - 성공 (Accept 상태)")
    void getAllShareInvitationsExcludingSelfByAccept() {
        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        List<FolderShareEntity> allInvitations = folderShareRepository.getAllShareInvitationsWithoutMe(myDefaultFolder.getFolderId(), myUser.getUserId());

        log.info("All Share Invitations Excluding Self: {}", allInvitations);

        Assertions.assertThat(allInvitations).hasSize(1);
        Assertions.assertThat(allInvitations.get(0).getTargetUser()).isEqualTo(otherUser);
        Assertions.assertThat(allInvitations.get(0).getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(allInvitations.get(0).getRole()).isEqualTo(editor);
        Assertions.assertThat(allInvitations.get(0).getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);
    }

    @Test
    @DisplayName("특정 폴더와 사용자로 폴더 공유 조회 - 성공 (Owner 권한)")
    void getShareInvitationByOwner() {
        Optional<FolderShareEntity> foundShare = folderShareRepository.getShareInvitation(
                myDefaultFolder.getFolderId(),
                myUser.getUserId()
        );

        log.info("Found Folder Share: {}", foundShare);

        Assertions.assertThat(foundShare).isPresent();
        Assertions.assertThat(foundShare.get().getShareId()).isNotNull();
        Assertions.assertThat(foundShare.get().getTargetUser()).isEqualTo(myUser);
        Assertions.assertThat(foundShare.get().getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(foundShare.get().getRole()).isEqualTo(owner);
        Assertions.assertThat(foundShare.get().getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);
    }

    @Test
    @DisplayName("특정 폴더와 사용자로 폴더 공유 조회 - 성공 (Accept 상태)")
    void getShareInvitation() {
        FolderShareEntity folderShare = folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        Optional<FolderShareEntity> foundShare = folderShareRepository.getShareInvitation(
                myDefaultFolder.getFolderId(),
                otherUser.getUserId()
        );

        log.info("Found Folder Share: {}", foundShare);

        Assertions.assertThat(foundShare).isPresent();
        Assertions.assertThat(foundShare.get().getShareId()).isEqualTo(folderShare.getShareId());
        Assertions.assertThat(foundShare.get().getTargetUser()).isEqualTo(otherUser);
        Assertions.assertThat(foundShare.get().getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(foundShare.get().getRole()).isEqualTo(editor);
        Assertions.assertThat(foundShare.get().getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);
    }

    @Test
    @DisplayName("특정 폴더와 사용자로 폴더 공유 조회 - 성공 (Pending 상태)")
    void getShareInvitationByPending() {
        FolderShareEntity folderShare = folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.PENDING)
                .create();

        Optional<FolderShareEntity> foundShare = folderShareRepository.getShareInvitation(
                myDefaultFolder.getFolderId(),
                otherUser.getUserId()
        );

        log.info("Found Folder Share: {}", foundShare);

        Assertions.assertThat(foundShare).isPresent();
        Assertions.assertThat(foundShare.get().getShareId()).isEqualTo(folderShare.getShareId());
        Assertions.assertThat(foundShare.get().getTargetUser()).isEqualTo(otherUser);
        Assertions.assertThat(foundShare.get().getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(foundShare.get().getRole()).isEqualTo(editor);
        Assertions.assertThat(foundShare.get().getInvitationStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    @DisplayName("특정 폴더와 사용자로 폴더 공유 조회 - 실패 (초대 없음)")
    void getShareInvitationByNoInvitation() {
        Optional<FolderShareEntity> foundShare = folderShareRepository.getShareInvitation(
                myDefaultFolder.getFolderId(),
                otherUser.getUserId()
        );

        log.info("Found Folder Share: {}", foundShare);

        Assertions.assertThat(foundShare).isEmpty();
    }

    @Test
    @DisplayName("특정 폴더와 사용자로 폴더 공유 조회 (Folder Join) - 성공")
    void getShareInvitationJoinFolder() {
        FolderShareEntity folderShare = folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        Optional<FolderShareEntity> foundShare = folderShareRepository.getShareInvitationWithFolder(
                myDefaultFolder.getFolderId(),
                otherUser.getUserId()
        );

        log.info("Found Folder Share with Join: {}", foundShare);

        Assertions.assertThat(foundShare).isPresent();
        Assertions.assertThat(foundShare.get().getShareId()).isEqualTo(folderShare.getShareId());
        Assertions.assertThat(foundShare.get().getTargetUser()).isEqualTo(otherUser);
        Assertions.assertThat(foundShare.get().getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(foundShare.get().getFolder().getFolderId()).isEqualTo(myDefaultFolder.getFolderId());
        Assertions.assertThat(foundShare.get().getFolder().getTitle()).isEqualTo(myDefaultFolder.getTitle());
        Assertions.assertThat(foundShare.get().getRole()).isEqualTo(editor);
        Assertions.assertThat(foundShare.get().getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);
    }

    @Test
    @DisplayName("특정 폴더와 사용자로 폴더 공유 조회 (Folder Join) - 성공 (Pending 상태)")
    void getShareInvitationJoinFolderByPending() {
        FolderShareEntity folderShare = folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(otherUser)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.PENDING)
                .create();

        Optional<FolderShareEntity> foundShare = folderShareRepository.getShareInvitationWithFolder(
                myDefaultFolder.getFolderId(),
                otherUser.getUserId()
        );

        log.info("Found Folder Share with Join: {}", foundShare);

        Assertions.assertThat(foundShare).isPresent();
        Assertions.assertThat(foundShare.get().getShareId()).isEqualTo(folderShare.getShareId());
        Assertions.assertThat(foundShare.get().getTargetUser()).isEqualTo(otherUser);
        Assertions.assertThat(foundShare.get().getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(foundShare.get().getRole()).isEqualTo(editor);
        Assertions.assertThat(foundShare.get().getInvitationStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    @DisplayName("특정 폴더와 사용자로 폴더 공유 조회 (Folder Join) - 성공 (Owner 권한)")
    void getShareInvitationJoinFolderByOwner() {
        Optional<FolderShareEntity> foundShare = folderShareRepository.getShareInvitationWithFolder(
                myDefaultFolder.getFolderId(),
                myUser.getUserId()
        );

        log.info("Found Folder Share with Join: {}", foundShare);

        Assertions.assertThat(foundShare).isPresent();
        Assertions.assertThat(foundShare.get().getShareId()).isNotNull();
        Assertions.assertThat(foundShare.get().getTargetUser()).isEqualTo(myUser);
        Assertions.assertThat(foundShare.get().getFolder()).isEqualTo(myDefaultFolder);
        Assertions.assertThat(foundShare.get().getFolder().getFolderId()).isEqualTo(myDefaultFolder.getFolderId());
        Assertions.assertThat(foundShare.get().getFolder().getTitle()).isEqualTo(myDefaultFolder.getTitle());
        Assertions.assertThat(foundShare.get().getRole()).isEqualTo(owner);
        Assertions.assertThat(foundShare.get().getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);
    }

    @Test
    @DisplayName("특정 폴더와 사용자로 폴더 공유 조회 (Folder Join) - 성공 (초대 없음)")
    void getShareInvitationJoinFolderByNoInvitation() {
        Optional<FolderShareEntity> foundShare = folderShareRepository.getShareInvitationWithFolder(
                myDefaultFolder.getFolderId(),
                otherUser.getUserId()
        );

        log.info("Found Folder Share with Join: {}", foundShare);

        Assertions.assertThat(foundShare).isEmpty();
    }
}
