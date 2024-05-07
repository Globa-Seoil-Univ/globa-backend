package org.y2k2.globa.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name="section")
@Table(name="section")
public class SectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id", columnDefinition = "INT UNSIGNED")
    private Long sectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "record_id", referencedColumnName = "record_id")
    private RecordEntity record;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "start_time", nullable = false)
    private Long startTime;

    @Column(name = "end_time", nullable = false)
    private Long endTime;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
