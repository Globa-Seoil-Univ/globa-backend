package org.y2k2.globa.infrastructure.persistence.dlq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="dlq")
public class DLQEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dlq_id", columnDefinition = "INT UNSIGNED")
    private Long dlqId;

    @Column(name = "step", nullable = false, length = 70)
    private String step;

    @Column(name = "type", nullable = false, length = 70)
    private String type;

    @Column(name = "message", nullable = false, length = 200)
    private String message;

    @Column(name = "occurrence_time", nullable = false)
    private LocalDateTime occurrenceTime;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
