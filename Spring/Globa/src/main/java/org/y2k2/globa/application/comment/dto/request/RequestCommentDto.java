package org.y2k2.globa.application.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

public record RequestCommentDto(
        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content
) {
}
