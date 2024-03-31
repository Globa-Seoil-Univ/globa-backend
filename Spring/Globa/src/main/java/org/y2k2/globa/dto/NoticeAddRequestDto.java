package org.y2k2.globa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;

import org.springframework.web.multipart.MultipartFile;

@Getter
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

    public NoticeAddRequestDto(String title, String content, MultipartFile thumbnail, String bgColor, Long[] imageIds) {
        this.title = title;
        this.content = content;
        this.thumbnail = thumbnail;
        this.bgColor = bgColor;
        this.imageIds = imageIds;
    }
}
