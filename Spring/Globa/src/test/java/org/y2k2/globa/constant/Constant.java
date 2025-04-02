package org.y2k2.globa.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Constant {
    JWT_HEADER("Authorization"),

    USER_PREFIX("/user"),
    ;

    private final String value;
}
