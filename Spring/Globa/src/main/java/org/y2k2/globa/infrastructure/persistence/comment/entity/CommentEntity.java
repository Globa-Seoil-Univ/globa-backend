package org.y2k2.globa.infrastructure.persistence.comment.entity;

import jakarta.persistence.*;

import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.*;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name="comment")
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", columnDefinition = "INT UNSIGNED")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "parent_id", columnDefinition = "INT UNSIGNED")
    private CommentEntity parent;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "highlight_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private HighlightEntity highlight;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT 0")
    private Boolean isDeleted;

    @CreationTimestamp
    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @Column(name = "deleted_time")
    private LocalDateTime deletedTime;

    public void updateContent(String content) {
        if (content != null) {
            this.content = content.trim();
        }
    }
}
