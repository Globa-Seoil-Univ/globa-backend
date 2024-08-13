package org.y2k2.globa.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestStudyDto {
    @NotNull(message = "You must request study field")
    @Min(0)
    private final Long studyTime;

    @NotBlank(message = "You must request createdTime field")
    private final String createdTime;
}
