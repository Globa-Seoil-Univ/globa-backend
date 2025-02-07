package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="study")
public class StudyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id", columnDefinition = "INT UNSIGNED")
    private Long studyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "record_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private RecordEntity record;

    @Column(name = "study_time", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Long studyTime;

    @CreationTimestamp
    @Column(name = "created_time", columnDefinition = "DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @Builder
    public StudyEntity(UserEntity user, RecordEntity record) {
        this.user = user;
        this.record = record;
    }
}