package org.y2k2.globa.application.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.web.multipart.MultipartFile;

public record RequestNoticeAddDto(
        @NotBlank(message = "제목은 필수 입력 사항입니다.")
        String title,

        @NotBlank(message = "내용은 필수 입력 사항입니다.")
        String content,

        @NotNull(message = "썸네일은 필수 입력 사항입니다.")
        MultipartFile thumbnail,

        @NotBlank(message = "배경색은 필수 입력 사항입니다.")
        String bgColor,

        Long[] imageIds
) {}
