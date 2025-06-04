package org.y2k2.globa.infrastructure.persistence.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.comment.projection.CommentWithHasReplyProjection;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;

import java.util.List;
import java.util.Optional;

public interface CommentJpaRepository extends JpaRepository<CommentEntity, Long> {
    Optional<CommentEntity> findByHighlight_HighlightIdAndCommentId(Long highlightId, Long commentId);
    Optional<CommentEntity> findByHighlight_HighlightIdAndCommentIdAndParentIsNull(Long highlightId, Long commentId);
    @Query(
            value = "SELECT c.commentId AS commentId, c.user.profilePath AS profilePath, c.user.name AS name, c.user.userId AS userId," +
                        "c.content AS content, c.isDeleted AS isDeleted, c.createdTime AS createdTime, " +
                        "CASE WHEN EXISTS (" +
                            "SELECT 1 FROM CommentEntity child WHERE child.parent = c" +
                        ") THEN TRUE ELSE FALSE END AS hasReply " +
                    "FROM CommentEntity c " +
                    "WHERE c.highlight.highlightId = :highlightId " +
                        "AND c.parent IS NULL " +
                    "ORDER BY c.commentId DESC "
    )
    Page<CommentWithHasReplyProjection> findByHighlight_HighlightIdAndParentIsNullOrderByCommentIdDesc(Long highlightId, Pageable pageable);
    Page<CommentEntity> findByParent_CommentIdOrderByCommentIdAsc(Long parentId, Pageable pageable);

    @Query(
            value = "SELECT CASE WHEN EXISTS ( " +
                        "SELECT TRUE FROM CommentEntity c " +
                        "WHERE c.highlight.highlightId = :highlightId " +
                        "AND c.commentId = :commentId " +
                        "AND c.parent IS NULL " +
                    ") THEN TRUE ELSE FALSE END"
    )
    Boolean isExistParentComment(Long highlightId, Long commentId);

    @Query(
            value = "SELECT CASE WHEN NOT EXISTS ( " +
                        "SELECT TRUE FROM CommentEntity c1 " +
                        "WHERE c1.highlight.highlightId = (" +
                            "SELECT c2.highlight.highlightId FROM CommentEntity c2 " +
                            "WHERE c2.commentId = :commentId" +
                        ") " +
                        "AND c1.isDeleted = FALSE " +
                        "AND c1.commentId != :commentId " +
                    ") THEN TRUE ELSE FALSE END"
    )
    Boolean existsSelfOrChildDeletedByCommentId(Long commentId);

    @Query(value = "SELECT comment_id, parent_id, highlight_id, " +
                    "user_id, content, is_deleted, created_time, deleted_time, false AS hasReply " +
                        "FROM comment " +
                        "WHERE (" +
                                    "highlight_id = ( " +
                                        "SELECT highlight_id FROM comment WHERE comment_id = :commentId " +
                                    ") AND is_deleted = TRUE" +
                                ")" +
                            "OR comment_id = :commentId", nativeQuery = true)
    List<CommentEntity> findAllSelfOrChildDeletedByCommentId(Long commentId);
}
