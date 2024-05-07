package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name="dictionary")
@Table(name="dictionary")
public class DictionaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dictionary_id", columnDefinition = "INT UNSIGNED")
    private Long dictionaryId;

    @Column(name = "word", nullable = false)
    private String word;

    @Column(name = "eng_word")
    private String engWord;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "pronunciation")
    private String pronunciation;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
