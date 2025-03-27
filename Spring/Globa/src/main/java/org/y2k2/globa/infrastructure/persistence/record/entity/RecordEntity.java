package org.y2k2.globa.infrastructure.persistence.record.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="record")
@DynamicInsert
public class RecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id", columnDefinition = "INT UNSIGNED")
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "folder_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private FolderEntity folder;

    @Column(name = "title", nullable = false, length = 32)
    private String title;

    @Column(name = "path", nullable = false, length = 300)
    private String path;

    @Column(name = "size", nullable = false, length = 100)
    private String size;

    @Column(name = "is_share", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isShare;

    @CreationTimestamp
    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}