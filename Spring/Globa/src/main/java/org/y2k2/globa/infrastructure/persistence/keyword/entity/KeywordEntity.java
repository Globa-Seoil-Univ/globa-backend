package org.y2k2.globa.infrastructure.persistence.keyword.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="keyword")
public class KeywordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id", columnDefinition = "INT UNSIGNED")
    private Long keywordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "record_id", nullable = false)
    private RecordEntity record;

    @Column(name = "word", nullable = false, length = 30)
    private String word;

    @Column(name = "importance", nullable = false, precision = 5, scale = 4)
    private BigDecimal importance;

    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}
