package org.y2k2.globa.domain.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.comment.projection.CommentWithHasReplyProjection;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    CommentEntity save(CommentEntity entity);
    List<CommentEntity> saveAll(List<CommentEntity> entities);
    void delete(CommentEntity entity);
    void deleteAll(List<CommentEntity> entities);

    Optional<CommentEntity> getComment(Long highlightId, Long commentId);
    Optional<CommentEntity> getParentComment(Long highlightId, Long commentId);

    Page<CommentWithHasReplyProjection> getParentComments(Long highlightId, Pageable pageable);
    Page<CommentEntity> getChildComments(Long parentId, Pageable pageable);

    List<CommentEntity> getAllDeletedComment(Long commentId);
    List<CommentEntity> getAllCleanupComment();

    Boolean isExistParentComment(Long highlightId, Long commentId);
    Boolean isLastAliveComment(Long commentId);
}
