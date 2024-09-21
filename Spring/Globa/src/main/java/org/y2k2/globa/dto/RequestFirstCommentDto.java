package org.y2k2.globa.dto;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class RequestFirstCommentDto {
    @NotNull(message = "You must request startIdx field")
    @Min(value = -1, message = "You must greater equal than 0")
    private final Long startIdx;

    @NotNull(message = "You must request endIdx field")
    @Min(value = -1, message = "You must greater equal than 0")
    private final Long endIdx;

    @NotBlank(message = "You must request content field")
    @Length(min = 1, message = "You must greater than 1 length")
    private final String content;

    @AssertTrue(message = "You must different startIdx and endIdx")
    private boolean isSame() {
        return !getStartIdx().equals(getEndIdx());
    }

    @AssertTrue(message = "You must startIdx greater than endIdx")
    private boolean isGreater() {
        return getStartIdx() < getEndIdx();
    }
}
