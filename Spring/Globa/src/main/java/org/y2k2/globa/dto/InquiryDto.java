package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InquiryDto {
    private long inquiryId;
    private String title;
    private String content;
    private String createdTime;
    private boolean isSolved;
}
