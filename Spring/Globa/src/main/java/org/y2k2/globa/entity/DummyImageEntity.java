package org.y2k2.globa.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "dummyImage")
@Table(name = "dummy_image")
public class DummyImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", columnDefinition = "INT UNSIGNED")
    private Long imageId;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "image_size", nullable = false, columnDefinition = "INT UNSIGNED")
    private Long imageSize;

    @Column(name = "image_type", nullable = false)
    private String imageType;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    public static DummyImageEntity create(String path, long size, String type) {
        DummyImageEntity entity = new DummyImageEntity();

        entity.setImagePath(path);
        entity.setImageSize(size);
        entity.setImageType(type);

        return entity;
    }
}
