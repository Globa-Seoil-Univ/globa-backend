package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ranking")
public class RankingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ranking_id", columnDefinition = "INT UNSIGNED")
    private Long rankingId;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED")
    private Long userId;
    @Column(name = "solved")
    private String solved;
}
