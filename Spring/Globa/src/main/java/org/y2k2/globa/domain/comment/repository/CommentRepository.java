package org.y2k2.globa.domain.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    CommentEntity save(CommentEntity entity);
    void delete(CommentEntity entity);
    void deleteAll(List<CommentEntity> entities);

    Optional<CommentEntity> getComment(Long commentId);
    Optional<CommentEntity> getParentComment(Long commentId);

    Page<CommentEntity> getParentComments(HighlightEntity highlight, Pageable pageable);
    Page<CommentEntity> getChildComments(Long parentId, Pageable pageable);
    List<CommentEntity> getAllDeletedComment(Long commentId);

    Boolean hasDeletedCommentInHighlight(Long commentId);
}
