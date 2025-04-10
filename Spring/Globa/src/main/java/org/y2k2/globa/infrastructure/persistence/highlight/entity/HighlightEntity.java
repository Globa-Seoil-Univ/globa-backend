package org.y2k2.globa.infrastructure.persistence.highlight.entity;

import jakarta.persistence.*;

import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.*;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name="highlight")
@Table(name="highlight")
public class HighlightEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "highlight_id", columnDefinition = "INT UNSIGNED")
    private Long highlightId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "section_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private SectionEntity section;

    @Column(name = "start_index", nullable = false, columnDefinition = "INT UNSIGNED")
    private Long startIndex;

    @Column(name = "end_index", nullable = false, columnDefinition = "INT UNSIGNED")
    private Long endIndex;

    @CreationTimestamp
    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}
