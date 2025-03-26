package org.y2k2.globa.application.comment.dto.request;

import jakarta.validation.constraints.*;

import org.hibernate.validator.constraints.Length;


public record RequestFirstCommentDto(
        @NotNull(message = "시작 인덱스는 필수입니다.")
        @Min(value = -1, message = "You must greater equal than 0")
        Long startIdx,

        @NotNull(message = "끝 인덱스는 필수입니다.")
        @Min(value = -1, message = "You must greater equal than 0")
        Long endIdx,

        @NotBlank(message = "내용은 필수입니다.")
        @Length(min = 1, message = "You must greater than 1 length")
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
