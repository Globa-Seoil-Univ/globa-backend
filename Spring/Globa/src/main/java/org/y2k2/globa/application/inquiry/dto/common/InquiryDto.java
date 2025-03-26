package org.y2k2.globa.application.inquiry.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InquiryDto {
    private Long inquiryId;
    private String title;
    private String content;
    private String createdTime;
    private Boolean isSolved;
}
