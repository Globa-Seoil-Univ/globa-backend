package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="dictionary")
public class DictionaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dictionary_id", columnDefinition = "INT UNSIGNED")
    private Long dictionaryId;

    @Column(name = "word", nullable = false, length = 100)
    private String word;

    @Column(name = "eng_word", length = 300)
    private String engWord;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "category", length = 20)
    private String category;

    @Column(name = "pronunciation", length = 300)
    private String pronunciation;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Builder
    public DictionaryEntity(String word, String engWord, String description, String category, String pronunciation) {
        this.word = word;
        this.engWord = engWord;
        this.description = description;
        this.category = category;
        this.pronunciation = pronunciation;
    }
}
