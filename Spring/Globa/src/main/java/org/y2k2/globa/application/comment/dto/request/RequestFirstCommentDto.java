package org.y2k2.globa.application.comment.dto.request;

import jakarta.validation.constraints.*;

import org.hibernate.validator.constraints.Length;


public record RequestFirstCommentDto(
        @NotNull(message = "시작 인덱스는 필수입니다.")
        @Positive(message = "시작 인덱스는 1 이상이어야 합니다.")
        Long startIdx,

        @NotNull(message = "끝 인덱스는 필수입니다.")
        @Positive(message = "끝 인덱스는 1 이상이어야 합니다.")
        @Max(value = 99999, message = "끝 인덱스는 99999 이하이어야 합니다.")
        Long endIdx,

        @NotBlank(message = "내용은 필수입니다.")
        @Length(min = 1, message = "내용은 최소 1자 이상이어야 합니다.")
        String content
) {
    @AssertTrue(message = "시작 인덱스와 끝 인덱스는 같을 수 없습니다.")
    private boolean isSame() {
        return !startIdx().equals(endIdx());
    }

    @AssertTrue(message = "시작 인덱스가 끝 인덱스보다 커야 합니다.")
    private boolean isGreater() {
        return startIdx() < endIdx();
    }
}
