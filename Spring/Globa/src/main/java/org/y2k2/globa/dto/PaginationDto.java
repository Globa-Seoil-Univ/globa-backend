package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.y2k2.globa.type.InquirySort;

@Getter
@AllArgsConstructor
public class PaginationDto {
    private int page;
    private int count;
    private InquirySort sort;
}
