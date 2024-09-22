package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.entity.CommentEntity;
import org.y2k2.globa.entity.HighlightEntity;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    CommentEntity findByCommentId(long commentId);
    long countByParentIsNullAndHighlight(HighlightEntity highlight);
    Page<CommentEntity> findByHighlightAndParentIsNullOrderByCreatedTimeDescCommentIdDesc(HighlightEntity highlight, Pageable pageable);
    Page<CommentEntity> findByParentOrderByCreatedTimeAscCommentIdAsc(CommentEntity parent, Pageable pageable);

    @Query(value = "SELECT NOT EXISTS (" +
                            "SELECT 1 " +
                                "FROM comment " +
                                "WHERE highlight_id = ( " +
                                    "SELECT highlight_id FROM comment WHERE comment_id = :commentId " +
                                ") " +
                                "AND deleted = FALSE " +
                                "AND comment_id != :commentId " +
                    ");", nativeQuery = true)
    Long existsSelfOrChildDeletedByCommentId(@Param("commentId") Long commentId);

    @Query(value = "SELECT comment_id, parent_id, highlight_id, " +
                    "user_id, content, deleted, created_time, deleted_time, false AS hasReply " +
                        "FROM comment " +
                        "WHERE highlight_id = ( " +
                            "SELECT highlight_id FROM comment WHERE comment_id = :commentId " +
                        ") AND deleted = TRUE OR comment_id = :commentId", nativeQuery = true)
    List<CommentEntity> findAllSelfOrChildDeletedByCommentId(@Param("commentId") Long commentId);
}
