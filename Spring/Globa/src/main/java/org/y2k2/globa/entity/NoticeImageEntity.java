package org.y2k2.globa.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "notice_image")
public class NoticeImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", columnDefinition = "INT UNSIGNED")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "notice_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private NoticeEntity notice;

    @Column(name = "image_path", nullable = false, length = 200)
    private String imagePath;

    @Column(name = "image_size", nullable = false, columnDefinition = "INT UNSIGNED")
    private Long imageSize;

    @Column(name = "image_type", nullable = false, length = 20)
    private String imageType;

    @CreationTimestamp
    @Column(name = "created_time", columnDefinition = "DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    public static NoticeImageEntity create(NoticeEntity notice, String path, long size, String type) {
        NoticeImageEntity entity = new NoticeImageEntity();

        entity.setNotice(notice);
        entity.setImagePath(path);
        entity.setImageSize(size);
        entity.setImageType(type);

        return entity;
    }
}
