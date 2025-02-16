package org.y2k2.globa.dto.request.folder;

import jakarta.validation.constraints.NotBlank;

public record RequestFolderNameDto(
        @NotBlank(message = "폴더 이름은 필수입니다.")
        String name
) {}
