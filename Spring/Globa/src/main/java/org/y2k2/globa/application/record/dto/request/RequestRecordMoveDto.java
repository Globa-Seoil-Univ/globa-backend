package org.y2k2.globa.application.record.dto.request;

import jakarta.validation.constraints.NotNull;

public record RequestRecordMoveDto(
        @NotNull(message = "이동할 대상의 폴더 ID는 필수입니다.")
        Long targetId
) {}
