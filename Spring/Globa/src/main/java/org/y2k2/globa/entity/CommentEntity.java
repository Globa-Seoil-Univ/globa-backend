package org.y2k2.globa.entity;

import jakarta.persistence.*;

import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name="comment")
@Table(name="comment")
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", columnDefinition = "INT UNSIGNED")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "comment_id")
    private CommentEntity parent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highlight_id", referencedColumnName = "highlight_id", nullable = false)
    private HighlightEntity highlight;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "deleted")
    @ColumnDefault("0")
    private boolean deleted;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "deleted_time")
    private LocalDateTime deletedTime;

    @Formula(value = "(SELECT CASE WHEN EXISTS (SELECT 1 FROM comment c WHERE c.parent_id = ce1_0.comment_id) THEN true ELSE false END)")
    private boolean hasReply;

    public static CommentEntity create(UserEntity writer, HighlightEntity highlight, String content) {
        CommentEntity entity = new CommentEntity();

        entity.setUser(writer);
        entity.setHighlight(highlight);
        entity.setContent(content);

        return entity;
    }

    public static CommentEntity createReply(UserEntity writer, HighlightEntity highlight, CommentEntity parent, String content) {
        CommentEntity entity = new CommentEntity();

        entity.setUser(writer);
        entity.setHighlight(highlight);
        entity.setParent(parent);
        entity.setContent(content);

        return entity;
    }
}
