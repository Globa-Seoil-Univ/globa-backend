package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "dumyImage")
@Table(name = "dumyImage")
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

    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}
