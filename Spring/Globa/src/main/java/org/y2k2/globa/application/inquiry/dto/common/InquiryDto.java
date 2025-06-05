package org.y2k2.globa.application.inquiry.dto.common;

public record InquiryDto(
        Long inquiryId,
        String title,
        String content,
        String createdTime,
        Boolean isSolved
) {
}
