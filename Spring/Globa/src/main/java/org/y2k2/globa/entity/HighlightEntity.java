package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "section_id", referencedColumnName = "section_id")
    private SectionEntity section;

    @Column(name = "start_index")
    private int startIndex;

    @Column(name = "end_index")
    private int endIndex;


    @Column(name = "type")
    private String type;


    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}
