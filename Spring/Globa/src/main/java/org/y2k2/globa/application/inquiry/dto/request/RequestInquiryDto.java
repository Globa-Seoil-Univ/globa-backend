package org.y2k2.globa.application.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RequestInquiryDto(
        @NotBlank(message = "You must request title field")
        String title,

        @NotBlank(message = "You must request content field")
        String content
) {}
