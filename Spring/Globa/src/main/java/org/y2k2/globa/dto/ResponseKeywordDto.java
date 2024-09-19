package org.y2k2.globa.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ResponseKeywordDto implements Serializable {
    private String word;
    private Double importance;

    @Builder
    public ResponseKeywordDto(String word, Double importance) {
        this.word = word;
        this.importance = importance;
    }
}