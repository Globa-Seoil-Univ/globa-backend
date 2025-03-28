package org.y2k2.globa.application.user.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

public record ResponseUserDto(
        String profile,
        String name,
        String code,
        Long userId,
        Long publicFolderId
) {
}