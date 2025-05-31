package org.y2k2.globa.infrastructure.persistence.comment.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.fixture.comment.CommentFixture;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.highlight.HighlightFixture;
import org.y2k2.globa.fixture.record.RecordFixture;
import org.y2k2.globa.fixture.section.SectionFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.config.RepositoryIntegrationTest;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
@RepositoryIntegrationTest
public class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private FolderRoleFixture folderRoleFixture;
    @Autowired
    private FolderShareFixture folderShareFixture;
    @Autowired
    private RecordFixture recordFixture;
    @Autowired
    private SectionFixture sectionFixture;
    @Autowired
    private HighlightFixture highlightFixture;
    @Autowired
    private CommentFixture commentFixture;

    private UserEntity myUser;
    private HighlightEntity highlight;

    @BeforeEach
    void setUp() {
        myUser = userFixture.create();
        FolderEntity myDefaultFolder = folderFixture
                .withUser(myUser)
                .create();
        FolderRoleEntity owner = folderRoleFixture
                .withRole(FolderRole.OWNER)
                .create();

        folderShareFixture
                .withFolder(myDefaultFolder)
                .withOwner(myUser)
                .withTarget(myUser)
                .withRole(owner)
                .withInvitationStatus(InvitationStatus.ACCEPT)
                .create();

        RecordEntity myRecord = recordFixture
                .withUser(myUser)
                .withFolder(myDefaultFolder)
                .create();
        SectionEntity section = sectionFixture
                .withRecord(myRecord)
                .create();
        highlight = highlightFixture
                .withSection(section)
                .create();
    }

    @Test
    @DisplayName("댓글 생성 - 성공")
    void createComment() {
        String content = "This is a comment.";

        CommentEntity comment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(content)
                .withDeleted(false)
                .create();

        CommentEntity savedComment = commentRepository.save(comment);

        log.info("Saved Comment = {}", savedComment.getCommentId());

        Assertions.assertThat(savedComment).isNotNull();
        Assertions.assertThat(savedComment.getContent()).isEqualTo(content);
        Assertions.assertThat(savedComment.getUser().getUserId()).isEqualTo(myUser.getUserId());
        Assertions.assertThat(savedComment.getHighlight().getHighlightId()).isEqualTo(highlight.getHighlightId());
        Assertions.assertThat(savedComment.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    void deleteComment() {
        String content = "This is a comment to be deleted.";

        CommentEntity comment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(content)
                .withDeleted(false)
                .create();

        CommentEntity savedComment = commentRepository.save(comment);
        log.info("Saved Comment = {}", savedComment.getCommentId());

        commentRepository.delete(savedComment);

        Assertions.assertThat(commentRepository.getComment(highlight.getHighlightId(), savedComment.getCommentId()))
                .isEmpty();
    }

    @Test
    @DisplayName("댓글 전체 삭제 - 성공")
    void deleteAllComments() {
        String content1 = "This is the first comment.";
        String content2 = "This is the second comment.";

        CommentEntity comment1 = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(content1)
                .withDeleted(false)
                .create();
        CommentEntity comment2 = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(content2)
                .withDeleted(false)
                .create();

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        commentRepository.deleteAll(List.of(comment1, comment2));

        Assertions.assertThat(commentRepository.getComment(highlight.getHighlightId(), comment1.getCommentId()))
                .isEmpty();
        Assertions.assertThat(commentRepository.getComment(highlight.getHighlightId(), comment2.getCommentId()))
                .isEmpty();
    }

    @Test
    @DisplayName("댓글 조회 - 성공")
    void getComment() {
        String content = "This is a comment to be retrieved.";

        CommentEntity comment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(content)
                .withDeleted(false)
                .create();

        CommentEntity savedComment = commentRepository.save(comment);
        log.info("Saved Comment = {}", savedComment.getCommentId());

        Optional<CommentEntity> retrievedComment = commentRepository.getComment(highlight.getHighlightId(), savedComment.getCommentId());

        Assertions.assertThat(retrievedComment).isPresent();
        Assertions.assertThat(retrievedComment).isNotNull();
        Assertions.assertThat(retrievedComment.get().getContent()).isEqualTo(content);
        Assertions.assertThat(retrievedComment.get().getUser().getUserId()).isEqualTo(myUser.getUserId());
        Assertions.assertThat(retrievedComment.get().getHighlight().getHighlightId()).isEqualTo(highlight.getHighlightId());
        Assertions.assertThat(retrievedComment.get().getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("댓글 조회 - 성공 (존재하지 않는 댓글)")
    void getCommentNotFound() {
        Long nonExistentHighlightId = 999L;
        Long nonExistentCommentId = 999L;

        Optional<CommentEntity> retrievedComment = commentRepository.getComment(nonExistentHighlightId, nonExistentCommentId);

        Assertions.assertThat(retrievedComment).isEmpty();
    }

    @Test
    @DisplayName("부모 댓글 조회 - 성공")
    void getParentComment() {
        String content = "This is a parent comment.";

        CommentEntity parentComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(content)
                .withDeleted(false)
                .create();

        CommentEntity savedParentComment = commentRepository.save(parentComment);
        log.info("Saved Parent Comment = {}", savedParentComment.getCommentId());

        Optional<CommentEntity> retrievedParentComment = commentRepository.getParentComment(highlight.getHighlightId(), savedParentComment.getCommentId());

        Assertions.assertThat(retrievedParentComment).isPresent();
        Assertions.assertThat(retrievedParentComment.get().getContent()).isEqualTo(content);
        Assertions.assertThat(retrievedParentComment.get().getUser().getUserId()).isEqualTo(myUser.getUserId());
        Assertions.assertThat(retrievedParentComment.get().getHighlight().getHighlightId()).isEqualTo(highlight.getHighlightId());
        Assertions.assertThat(retrievedParentComment.get().getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("부모 댓글 조회 - 성공 (존재하지 않는 댓글)")
    void getParentCommentNotFound() {
        Long nonExistentHighlightId = 999L;
        Long nonExistentCommentId = 999L;

        Optional<CommentEntity> retrievedParentComment = commentRepository.getParentComment(nonExistentHighlightId, nonExistentCommentId);

        Assertions.assertThat(retrievedParentComment).isEmpty();
    }

    @Test
    @DisplayName("부모 댓글 목록 조회 - 성공")
    void getParentComments() {
        String content1 = "This is the first parent comment.";
        String content2 = "This is the second parent comment.";

        CommentEntity parentComment1 = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(content1)
                .withDeleted(false)
                .create();
        CommentEntity parentComment2 = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(content2)
                .withDeleted(false)
                .create();

        commentRepository.save(parentComment1);
        commentRepository.save(parentComment2);

        Pageable pageable = Pageable.ofSize(10);
        Page<CommentEntity> parentCommentsPage = commentRepository.getParentComments(highlight.getHighlightId(), pageable);

        Assertions.assertThat(parentCommentsPage).isNotNull();
        Assertions.assertThat(parentCommentsPage.getTotalElements()).isEqualTo(2);

        log.info("Parent Comments = {}", parentCommentsPage.getContent().stream()
                .map(CommentEntity::getCommentId)
                .toList());

        Assertions.assertThat(parentCommentsPage.getContent())
                .allSatisfy(comment -> {
                    Assertions.assertThat(comment.getContent()).isIn(content1, content2);
                    Assertions.assertThat(comment.getUser().getUserId()).isEqualTo(myUser.getUserId());
                    Assertions.assertThat(comment.getHighlight().getHighlightId()).isEqualTo(highlight.getHighlightId());
                    Assertions.assertThat(comment.getIsDeleted()).isFalse();
                });
    }

    @Test
    @DisplayName("자식 댓글 목록 조회 - 성공")
    void getChildComments() {
        String parentContent = "This is a parent comment.";
        String childContent1 = "This is the first child comment.";
        String childContent2 = "This is the second child comment.";

        CommentEntity parentComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(parentContent)
                .withDeleted(false)
                .create();

        CommentEntity savedParentComment = commentRepository.save(parentComment);
        log.info("Saved Parent Comment = {}", savedParentComment.getCommentId());

        CommentEntity childComment1 = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withParent(savedParentComment)
                .withContent(childContent1)
                .withDeleted(false)
                .create();
        CommentEntity childComment2 = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withParent(savedParentComment)
                .withContent(childContent2)
                .withDeleted(false)
                .create();

        commentRepository.save(childComment1);
        commentRepository.save(childComment2);

        Pageable pageable = Pageable.ofSize(10);
        Page<CommentEntity> childCommentsPage = commentRepository.getChildComments(savedParentComment.getCommentId(), pageable);

        Assertions.assertThat(childCommentsPage).isNotNull();
        Assertions.assertThat(childCommentsPage.getTotalElements()).isEqualTo(2);

        log.info("Child Comments = {}", childCommentsPage.getContent().stream()
                .map(CommentEntity::getCommentId)
                .toList());

        Assertions.assertThat(childCommentsPage.getContent())
                .allSatisfy(comment -> {
                    Assertions.assertThat(comment.getContent()).isIn(childContent1, childContent2);
                    Assertions.assertThat(comment.getUser().getUserId()).isEqualTo(myUser.getUserId());
                    Assertions.assertThat(comment.getHighlight().getHighlightId()).isEqualTo(highlight.getHighlightId());
                    Assertions.assertThat(comment.getIsDeleted()).isFalse();
                    Assertions.assertThat(comment.getParent().getCommentId()).isEqualTo(savedParentComment.getCommentId());
                });
    }

    @Test
    @DisplayName("자식 댓글 목록 조회 - 성공 (존재하지 않는 부모 댓글)")
    void getChildCommentsNotFound() {
        Long nonExistentParentCommentId = 999L;
        Pageable pageable = Pageable.ofSize(10);

        Page<CommentEntity> childCommentsPage = commentRepository.getChildComments(nonExistentParentCommentId, pageable);

        Assertions.assertThat(childCommentsPage).isNotNull();
        Assertions.assertThat(childCommentsPage.getTotalElements()).isEqualTo(0);
        Assertions.assertThat(childCommentsPage.getContent()).isEmpty();
    }

    @Test
    @DisplayName("자신이 속한 삭제된 댓글 조회 - 성공 (자신 포함)")
    void getAllDeletedComment() {
        String content = "This is a comment to be deleted.";

        CommentEntity comment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(content)
                .withDeleted(true)
                .create();

        CommentEntity savedComment = commentRepository.save(comment);
        log.info("Saved Comment = {}", savedComment.getCommentId());

        List<CommentEntity> deletedComments = commentRepository.getAllDeletedComment(savedComment.getCommentId());

        Assertions.assertThat(deletedComments).isNotEmpty();
        Assertions.assertThat(deletedComments).hasSize(1);
        Assertions.assertThat(deletedComments.get(0).getContent()).isEqualTo(content);
        Assertions.assertThat(deletedComments.get(0).getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("자신이 속한 삭제된 댓글 조회 - 성공 (자신 포함, 부모 기준)")
    void getAllDeletedCommentWithChildren() {
        String parentContent = "This is a parent comment.";
        String childContent = "This is a child comment.";

        CommentEntity parentComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(parentContent)
                .withDeleted(false)
                .create();

        CommentEntity savedParentComment = commentRepository.save(parentComment);
        log.info("Saved Parent Comment = {}", savedParentComment.getCommentId());

        CommentEntity childComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withParent(savedParentComment)
                .withContent(childContent)
                .withDeleted(true)
                .create();

        commentRepository.save(childComment);

        List<CommentEntity> deletedComments = commentRepository.getAllDeletedComment(savedParentComment.getCommentId());

        Assertions.assertThat(deletedComments).isNotEmpty();
        Assertions.assertThat(deletedComments).hasSize(2);
        Assertions.assertThat(deletedComments.stream().anyMatch(c -> c.getContent().equals(parentContent))).isTrue();
        Assertions.assertThat(deletedComments.stream().anyMatch(c -> c.getContent().equals(childContent))).isTrue();
    }

    @Test
    @DisplayName("자신이 속한 삭제된 댓글 조회 - 성공 (자신 포함, 자식 기준)")
    void getAllDeletedCommentWithParent() {
        String parentContent = "This is a parent comment.";
        String childContent = "This is a child comment.";

        CommentEntity parentComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(parentContent)
                .withDeleted(true)
                .create();

        CommentEntity savedParentComment = commentRepository.save(parentComment);
        log.info("Saved Parent Comment = {}", savedParentComment.getCommentId());

        CommentEntity childComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withParent(savedParentComment)
                .withContent(childContent)
                .withDeleted(false)
                .create();

        CommentEntity savedChildComment = commentRepository.save(childComment);

        List<CommentEntity> deletedComments = commentRepository.getAllDeletedComment(savedChildComment.getCommentId());

        Assertions.assertThat(deletedComments).isNotEmpty();
        Assertions.assertThat(deletedComments).hasSize(2);
        Assertions.assertThat(deletedComments.stream().anyMatch(c -> c.getContent().equals(parentContent))).isTrue();
        Assertions.assertThat(deletedComments.stream().anyMatch(c -> c.getContent().equals(childContent))).isTrue();
    }

    @Test
    @DisplayName("자신이 속한 삭제된 댓글 조회 - 성공 (존재하지 않는 댓글)")
    void getAllDeletedCommentNotFound() {
        Long nonExistentCommentId = 999L;

        List<CommentEntity> deletedComments = commentRepository.getAllDeletedComment(nonExistentCommentId);

        Assertions.assertThat(deletedComments).isEmpty();
    }

    @Test
    @DisplayName("마지막 댓글 - 성공 (자신 기준)")
    void isLastAliveComment() {
        String content = "This is a comment to check if it's the last alive comment.";

        CommentEntity comment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(content)
                .withDeleted(false)
                .create();

        CommentEntity savedComment = commentRepository.save(comment);
        log.info("Saved Comment = {}", savedComment.getCommentId());

        Boolean isLastAlive = commentRepository.isLastAliveComment(savedComment.getCommentId());

        Assertions.assertThat(isLastAlive).isTrue();
    }

    @Test
    @DisplayName("마지막 댓글 - 성공 (부모 기준, 자식 댓글이 삭제된 경우)")
    void isLastAliveCommentWithChildren() {
        String parentContent = "This is a parent comment.";
        String childContent = "This is a child comment.";

        CommentEntity parentComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(parentContent)
                .withDeleted(false)
                .create();

        CommentEntity savedParentComment = commentRepository.save(parentComment);
        log.info("Saved Parent Comment = {}", savedParentComment.getCommentId());

        CommentEntity childComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withParent(savedParentComment)
                .withContent(childContent)
                .withDeleted(true)
                .create();

        commentRepository.save(childComment);

        Boolean isLastAlive = commentRepository.isLastAliveComment(savedParentComment.getCommentId());

        Assertions.assertThat(isLastAlive).isTrue();
    }

    @Test
    @DisplayName("마지막 댓글 - 성공 (부모 기준, 자식 댓글이 삭제되지 않은 경우)")
    void isLastAliveCommentWithNonDeletedChildren() {
        String parentContent = "This is a parent comment.";
        String childContent = "This is a child comment.";

        CommentEntity parentComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(parentContent)
                .withDeleted(false)
                .create();

        CommentEntity savedParentComment = commentRepository.save(parentComment);
        log.info("Saved Parent Comment = {}", savedParentComment.getCommentId());

        CommentEntity childComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withParent(savedParentComment)
                .withContent(childContent)
                .withDeleted(false)
                .create();

        commentRepository.save(childComment);

        Boolean isLastAlive = commentRepository.isLastAliveComment(savedParentComment.getCommentId());

        Assertions.assertThat(isLastAlive).isFalse();
    }

    @Test
    @DisplayName("마지막 댓글 - 성공 (자식 기준, 부모 댓글이 삭제된 경우)")
    void isLastAliveCommentWithDeletedParent() {
        String parentContent = "This is a parent comment.";
        String childContent = "This is a child comment.";

        CommentEntity parentComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(parentContent)
                .withDeleted(true)
                .create();

        CommentEntity savedParentComment = commentRepository.save(parentComment);
        log.info("Saved Parent Comment = {}", savedParentComment.getCommentId());

        CommentEntity childComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withParent(savedParentComment)
                .withContent(childContent)
                .withDeleted(false)
                .create();

        commentRepository.save(childComment);

        Boolean isLastAlive = commentRepository.isLastAliveComment(childComment.getCommentId());

        Assertions.assertThat(isLastAlive).isTrue();
    }

    @Test
    @DisplayName("마지막 댓글 - 성공 (자식 기준, 부모 댓글이 삭제되지 않은 경우)")
    void isLastAliveCommentWithNonDeletedParent() {
        String parentContent = "This is a parent comment.";
        String childContent = "This is a child comment.";

        CommentEntity parentComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(parentContent)
                .withDeleted(false)
                .create();

        CommentEntity savedParentComment = commentRepository.save(parentComment);
        log.info("Saved Parent Comment = {}", savedParentComment.getCommentId());

        CommentEntity childComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withParent(savedParentComment)
                .withContent(childContent)
                .withDeleted(false)
                .create();

        commentRepository.save(childComment);

        Boolean isLastAlive = commentRepository.isLastAliveComment(childComment.getCommentId());

        Assertions.assertThat(isLastAlive).isFalse();
    }

    @Test
    @DisplayName("마지막 댓글 - 성공 (부모 기준, 자식 댓글이 모두 삭제되지 않은 경우)")
    void isLastAliveCommentWithNonDeletedAllChildren() {
        String parentContent = "This is a parent comment.";
        String childContent1 = "This is the first child comment.";
        String childContent2 = "This is the second child comment.";

        CommentEntity parentComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(parentContent)
                .withDeleted(false)
                .create();

        CommentEntity savedParentComment = commentRepository.save(parentComment);
        log.info("Saved Parent Comment = {}", savedParentComment.getCommentId());

        CommentEntity childComment1 = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withParent(savedParentComment)
                .withContent(childContent1)
                .withDeleted(true) // Deleted Comment
                .create();
        CommentEntity childComment2 = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withParent(savedParentComment)
                .withContent(childContent2)
                .withDeleted(false)
                .create();

        commentRepository.save(childComment1);
        commentRepository.save(childComment2);

        Boolean isLastAlive = commentRepository.isLastAliveComment(savedParentComment.getCommentId());

        Assertions.assertThat(isLastAlive).isFalse();
    }

    @Test
    @DisplayName("마지막 댓글 - 성공 (자식 기준, 부모 댓글과 자식 댓글이 모두 삭제되지 않은 경우)")
    void isLastAliveCommentWithNonDeletedParentAndChildren() {
        String parentContent = "This is a parent comment.";
        String childContent1 = "This is the first child comment.";
        String childContent2 = "This is the second child comment.";

        CommentEntity parentComment = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withContent(parentContent)
                .withDeleted(false)
                .create();

        CommentEntity savedParentComment = commentRepository.save(parentComment);
        log.info("Saved Parent Comment = {}", savedParentComment.getCommentId());

        CommentEntity childComment1 = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withParent(savedParentComment)
                .withContent(childContent1)
                .withDeleted(false)
                .create();
        CommentEntity childComment2 = commentFixture
                .withUser(myUser)
                .withHighlight(highlight)
                .withParent(savedParentComment)
                .withContent(childContent2)
                .withDeleted(false)
                .create();

        commentRepository.save(childComment1);
        commentRepository.save(childComment2);

        Boolean isLastAlive = commentRepository.isLastAliveComment(childComment1.getCommentId());

        Assertions.assertThat(isLastAlive).isFalse();
    }
}
