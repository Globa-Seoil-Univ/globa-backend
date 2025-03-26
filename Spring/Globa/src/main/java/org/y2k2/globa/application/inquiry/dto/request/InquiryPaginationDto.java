package org.y2k2.globa.application.inquiry.dto.request;

import org.y2k2.globa.common.type.InquirySort;

public record InquiryPaginationDto(
        int page,
        int count,
        InquirySort sort
) {}
