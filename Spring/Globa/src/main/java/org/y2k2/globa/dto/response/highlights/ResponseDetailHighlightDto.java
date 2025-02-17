package org.y2k2.globa.dto.response.highlights;

public record ResponseDetailHighlightDto(
        Long highlightId,
        Character type,
        Long startIndex,
        Long endIndex
) {}
