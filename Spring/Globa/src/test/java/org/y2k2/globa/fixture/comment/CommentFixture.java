package org.y2k2.globa.fixture.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.comment.CommentFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Component
public class CommentFixture extends AbstractFixture<CommentEntity> {
    @Autowired
    private CommentFactory commentFactory;

    @Override
    protected CommentEntity build() {
        return commentFactory.createAndSave();
    }

    public CommentFixture withContent(String content) {
        commentFactory.setContent(content);
        return this;
    }

    public CommentFixture withHighlight(HighlightEntity highlight) {
        commentFactory.setHighlight(highlight);
        return this;
    }

    public CommentFixture withParent(CommentEntity parent) {
        commentFactory.setParent(parent);
        return this;
    }

    public CommentFixture withUser(UserEntity user) {
        commentFactory.setUser(user);
        return this;
    }

    public CommentFixture withDeleted(boolean isDeleted) {
        commentFactory.setDeleted(isDeleted);
        return this;
    }
}
