package org.y2k2.globa.application.inquiry.dto.response;

import org.y2k2.globa.application.inquiry.dto.common.InquiryDto;

import java.util.List;

public record ResponseInquiryDto(
        List<InquiryDto> inquires,
        Long total
) {
}
