package org.y2k2.globa.entity;

import jakarta.persistence.*;

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
    @JoinColumn(name = "section_id", referencedColumnName = "section_id")
    private SectionEntity section;

    @Column(name = "start_index", nullable = false)
    private Long startIndex;

    @Column(name = "end_index", nullable = false)
    private Long endIndex;

    @Column(name = "type", nullable = false)
    @ColumnDefault("1")
    @Check(constraints = "type IN ('1', '2')")
    private Character type;

    @CreationTimestamp
    @Column(name = "created_time")
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
