package org.y2k2.globa.application.inquiry.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.application.answer.dto.common.AnswerDto;

@Getter
@Setter
@AllArgsConstructor
public class ResponseInquiryDetailDto {
    private String title;
    private String content;
    private String createdTime;
    private AnswerDto answer;
}
