package org.y2k2.globa.application.inquiry.dto.response;

import org.y2k2.globa.application.answer.dto.common.AnswerDto;

public record ResponseInquiryDetailDto(
        String title,
        String content,
        String createdTime,
        AnswerDto answer
) {
}
