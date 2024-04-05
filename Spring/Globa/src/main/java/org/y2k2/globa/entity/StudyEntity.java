package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity(name="study")
@Table(name="study")
public class StudyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id", columnDefinition = "INT UNSIGNED")
    private Long studyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "record_id", referencedColumnName = "record_id")
    private RecordEntity record;

    @Column(name = "study_time", columnDefinition = "INT UNSIGNED")
    private Long studyTime;


    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}