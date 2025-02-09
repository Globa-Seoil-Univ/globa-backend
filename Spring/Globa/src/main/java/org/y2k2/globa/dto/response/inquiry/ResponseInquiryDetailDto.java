package org.y2k2.globa.dto.response.inquiry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.dto.common.answer.AnswerDto;

@Getter
@Setter
@AllArgsConstructor
public class ResponseInquiryDetailDto {
    private String title;
    private String content;
    private String createdTime;
    private AnswerDto answer;
}
