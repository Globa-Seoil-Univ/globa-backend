package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.y2k2.globa.entity.CommentEntity;
import org.y2k2.globa.entity.HighlightEntity;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    CommentEntity findByCommentId(long commentId);
    long countByParentIsNullAndHighlight(HighlightEntity highlight);
    Page<CommentEntity> findByHighlightAndParentIsNullOrderByCreatedTimeDescCommentIdDesc(HighlightEntity highlight, Pageable pageable);
    Page<CommentEntity> findByParentOrderByCreatedTimeAscCommentIdAsc(CommentEntity parent, Pageable pageable);
}
