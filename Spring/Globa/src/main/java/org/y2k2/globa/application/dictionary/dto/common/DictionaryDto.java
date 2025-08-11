package org.y2k2.globa.application.dictionary.dto.common;

import lombok.Builder;

@Builder
public record DictionaryDto(
        String word,
        String engWord,
        String description,
        String category,
        String pronunciation
) {}