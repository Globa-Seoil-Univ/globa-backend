package org.y2k2.globa.application.hightlight.dto.response;

public record ResponseDetailHighlightDto(
        Long highlightId,
        Character type,
        Long startIndex,
        Long endIndex
) {}
