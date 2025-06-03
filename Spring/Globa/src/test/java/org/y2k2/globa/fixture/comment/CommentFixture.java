package org.y2k2.globa.fixture.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.comment.repository.CommentRepository;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.comment.repository.CommentRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Import(CommentRepositoryImpl.class)
@Component
public class CommentFixture implements Fixture<CommentEntity> {
    @Autowired
    private CommentRepository commentRepository;

    public static CommentBuilder builder() {
        return new CommentBuilder();
    }

    @Override
    public CommentEntity save(CommentEntity entity) {
        return commentRepository.save(entity);
    }

    public static class CommentBuilder {
        private String content = "Default comment content";
        private HighlightEntity highlight;
        private CommentEntity parent;
        private UserEntity user;
        private boolean isDeleted = false;

        private CommentBuilder() {}

        public CommentBuilder content(String content) {
            this.content = content;
            return this;
        }

        public CommentBuilder highlight(HighlightEntity highlight) {
            this.highlight = highlight;
            return this;
        }

        public CommentBuilder parent(CommentEntity parent) {
            this.parent = parent;
            return this;
        }

        public CommentBuilder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public CommentBuilder deleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public CommentEntity build() {
            CommentEntity entity = new CommentEntity();
            entity.setContent(this.content);
            entity.setHighlight(this.highlight);
            entity.setParent(this.parent);
            entity.setUser(this.user);
            entity.setIsDeleted(this.isDeleted);
            return entity;
        }
    }
}
