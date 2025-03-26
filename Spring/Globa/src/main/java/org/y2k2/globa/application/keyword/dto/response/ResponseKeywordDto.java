package org.y2k2.globa.application.keyword.dto.response;

import lombok.Builder;

public record ResponseKeywordDto(
        String word,
        Double importance
) {
    @Builder
    public ResponseKeywordDto(String word, Double importance) {
        this.word = word;
        this.importance = importance;
    }
}