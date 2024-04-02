package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserIntroDto {
    private final Long userId;
    private final String profile;
    private final String name;
}
