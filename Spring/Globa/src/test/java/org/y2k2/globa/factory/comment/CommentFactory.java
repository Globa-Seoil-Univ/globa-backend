package org.y2k2.globa.factory.comment;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.factory.AbstractFactory;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.comment.repository.CommentRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
@Getter
@Setter
@Import(CommentRepositoryImpl.class)
@Component
public class CommentFactory extends AbstractFactory<CommentEntity> {
    @Getter
    @Setter
    @Autowired
    private CommentRepository commentRepository;

    private String content = "content";
    private HighlightEntity highlight;
    private CommentEntity parent;
    private UserEntity user;
    private boolean isDeleted = false;

    @Override
    protected CommentEntity create() {
        return new CommentEntity();
    }

    @Override
    protected CommentEntity setDefaultValues(CommentEntity entity) {
        entity.setContent(content);
        entity.setHighlight(highlight);
        entity.setParent(parent);
        entity.setUser(user);
        entity.setIsDeleted(isDeleted);
        return entity;
    }

    @Override
    protected CommentEntity saveEntity(CommentEntity entity) {
        if (commentRepository != null) {
            return commentRepository.save(entity);
        } else {
            throw new RuntimeException("CommentRepository is null, entity will not be persisted");
        }
    }
}
