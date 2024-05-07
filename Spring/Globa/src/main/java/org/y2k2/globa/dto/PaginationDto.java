package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaginationDto {
    private int page;
    private int count;
    private SortDto sort;
}
