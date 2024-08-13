package org.y2k2.globa.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ResponseDetailHighlightDto {
    private Long highlightId;
    private Character type;
    private Long startIndex;
    private Long endIndex;
}
