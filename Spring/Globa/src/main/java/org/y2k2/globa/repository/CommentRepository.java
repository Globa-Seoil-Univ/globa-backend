package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.entity.CommentEntity;
import org.y2k2.globa.entity.HighlightEntity;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    Optional<CommentEntity> findByCommentId(Long commentId);
    Optional<CommentEntity> findByCommentIdAndParentIsNull(Long commentId);
    Page<CommentEntity> findByHighlightAndParentIsNullOrderByCommentIdDesc(HighlightEntity highlight, Pageable pageable);
    Page<CommentEntity> findByParent_CommentIdOrderByCommentIdAsc(Long parentId, Pageable pageable);

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
                        "WHERE highlight_id = ( " +
                            "SELECT highlight_id FROM comment WHERE comment_id = :commentId " +
                        ") AND is_deleted = TRUE OR comment_id = :commentId", nativeQuery = true)
    List<CommentEntity> findAllSelfOrChildDeletedByCommentId(Long commentId);
}
