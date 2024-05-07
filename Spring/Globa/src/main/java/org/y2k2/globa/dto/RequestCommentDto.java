package org.y2k2.globa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@AllArgsConstructor
@Builder
@Jacksonized
public class RequestCommentDto {
    @NotBlank(message = "You must request content field")
    private final String content;
}
