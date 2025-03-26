package org.y2k2.globa.application.answer.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AnswerDto {
    private String title;
    private String content;
    private String createdTime;
}
