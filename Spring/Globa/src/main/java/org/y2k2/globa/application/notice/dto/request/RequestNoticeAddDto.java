package org.y2k2.globa.application.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.web.multipart.MultipartFile;

public record RequestNoticeAddDto(
        @NotBlank(message = "You must request title field")
        String title,

        @NotBlank(message = "You must request content field")
        String content,

        @NotNull(message = "You must request thumbnail field")
        MultipartFile thumbnail,

        @NotBlank(message = "You must request bgColor field")
        String bgColor,

        Long[] imageIds
) {}
