package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "record_id", referencedColumnName = "record_id")
    private RecordEntity record;

    @Column(name = "title")
    private String title;

    @Column(name = "start_time")
    private int startTime;

    @Column(name = "end_time")
    private int entTime;

    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}
