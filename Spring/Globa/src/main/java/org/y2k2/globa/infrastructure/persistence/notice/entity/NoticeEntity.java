package org.y2k2.globa.infrastructure.persistence.notice.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name="notice")
@Table(name="notice")
public class NoticeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id", columnDefinition = "INT UNSIGNED")
    private Long noticeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "user_id", columnDefinition = "INT UNSIGNED")
    private UserEntity user;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "thumbnail_path", nullable = false, length = 200)
    private String thumbnailPath;

    @Column(name = "thumbnail_size", nullable = false, columnDefinition = "INT UNSIGNED")
    private Long thumbnailSize;

    @Column(name = "thumbnail_type", nullable = false, length = 20)
    private String thumbnailType;

    @Column(name = "bg_color", nullable = false, length = 9)
    private String bgColor;

    @CreationTimestamp
    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}
