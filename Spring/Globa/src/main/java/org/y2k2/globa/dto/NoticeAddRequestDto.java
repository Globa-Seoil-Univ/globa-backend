package org.y2k2.globa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class NoticeAddRequestDto {
    @NotBlank(message = "You must request title field")
    private final String title;
    @NotBlank(message = "You must request content field")
    private final String content;
    @NotNull(message = "You must request thumbnail field")
    private final MultipartFile thumbnail;
    @NotBlank(message = "You must request bgColor field")
    private final String bgColor;
    private final Long[] imageIds;
}
