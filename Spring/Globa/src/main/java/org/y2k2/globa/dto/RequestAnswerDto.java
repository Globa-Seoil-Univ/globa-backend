package org.y2k2.globa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestAnswerDto {
    @NotBlank(message = "You must request title field")
    private final String title;
    @NotBlank(message = "You must request content field")
    private final String content;
}
