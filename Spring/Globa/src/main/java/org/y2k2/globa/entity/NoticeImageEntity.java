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
@Entity(name = "noticeImage")
@Table(name = "notice_image")
public class NoticeImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", columnDefinition = "INT UNSIGNED")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "notice_id", referencedColumnName = "notice_id")
    private NoticeEntity notice;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "image_size", nullable = false, columnDefinition = "INT UNSIGNED")
    private Long imageSize;

    @Column(name = "image_type", nullable = false)
    private String imageType;

    @CreationTimestamp
    @Column(name = "created_time")
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
