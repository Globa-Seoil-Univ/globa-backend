package org.y2k2.globa.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ResponseDetailHighlightDto {
    private Long highlightId;
    private String type;
    private int startIndex;
    private int endIndex;
}
