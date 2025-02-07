package org.y2k2.globa.entity;

import jakarta.persistence.*;

import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.*;

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

    @Column(name = "type", nullable = false, columnDefinition = "DEFAULT 1")
    private Character type;

    @CreationTimestamp
    @Column(name = "created_time", columnDefinition = "DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    public static HighlightEntity create(SectionEntity section, long startIndex, long endIndex) {
        HighlightEntity entity = new HighlightEntity();

        entity.setSection(section);
        entity.setStartIndex(startIndex);
        entity.setEndIndex(endIndex);
        entity.setType('2');

        return entity;
    }
}
