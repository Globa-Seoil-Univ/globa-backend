package org.y2k2.globa.entity;

import jakarta.persistence.*;

import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="section")
public class SectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id", columnDefinition = "INT UNSIGNED")
    private Long sectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "record_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private RecordEntity record;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "start_time", nullable = false, columnDefinition = "INT UNSIGNED")
    private Long startTime;

    @Column(name = "end_time", nullable = false, columnDefinition = "INT UNSIGNED")
    private Long endTime;

    @CreationTimestamp
    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}
