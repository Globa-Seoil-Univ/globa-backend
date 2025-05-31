package org.y2k2.globa.infrastructure.persistence.comment.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CommentRepositoryImpl implements CommentRepository {
    private final CommentJpaRepository commentJpaRepository;

    @Override
    public CommentEntity save(CommentEntity entity) {
        return commentJpaRepository.save(entity);
    }

    @Override
    public void delete(CommentEntity entity) {
        commentJpaRepository.delete(entity);
    }

    @Override
    public void deleteAll(List<CommentEntity> entities) {
        commentJpaRepository.deleteAllInBatch(entities);
    }

    @Override
    public Optional<CommentEntity> getComment(Long highlightId, Long commentId) {
        return commentJpaRepository.findByHighlight_HighlightIdAndCommentId(highlightId, commentId);
    }

    @Override
    public Optional<CommentEntity> getParentComment(Long highlightId, Long commentId) {
        return commentJpaRepository.findByHighlight_HighlightIdAndCommentIdAndParentIsNull(highlightId, commentId);
    }

    @Override
    public Page<CommentEntity> getParentComments(Long highlightId, Pageable pageable) {
        return commentJpaRepository.findByHighlight_HighlightIdAndParentIsNullOrderByCommentIdDesc(highlightId, pageable);
    }

    @Override
    public Page<CommentEntity> getChildComments(Long parentId, Pageable pageable) {
        return commentJpaRepository.findByParent_CommentIdOrderByCommentIdAsc(parentId, pageable);
    }

    @Override
    public List<CommentEntity> getAllDeletedComment(Long commentId) {
        return commentJpaRepository.findAllSelfOrChildDeletedByCommentId(commentId);
    }

    @Override
    public Boolean isExistParentComment(Long highlightId, Long commentId) {
        return commentJpaRepository.isExistParentComment(highlightId, commentId);
    }

    @Override
    public Boolean isLastAliveComment(Long commentId) {
        return commentJpaRepository.existsSelfOrChildDeletedByCommentId(commentId);
    }
}
