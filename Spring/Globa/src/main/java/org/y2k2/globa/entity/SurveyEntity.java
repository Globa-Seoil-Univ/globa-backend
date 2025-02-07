package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name="survey")
@Table(name="survey")
public class SurveyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "survey_id", columnDefinition = "INT UNSIGNED")
    private Long surveyId;

    @Column(name = "survey_type", columnDefinition = "CHAR(1)")
    private Character surveyType;

    @Lob
    @Column(name = "content")
    private String content;

    @CreationTimestamp
    @Column(name = "created_time", columnDefinition = "DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}