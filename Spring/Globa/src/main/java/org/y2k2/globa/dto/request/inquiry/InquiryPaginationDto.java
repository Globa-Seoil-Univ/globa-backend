package org.y2k2.globa.dto.request.inquiry;

import org.y2k2.globa.type.InquirySort;

public record InquiryPaginationDto(
        int page,
        int count,
        InquirySort sort
) {}
