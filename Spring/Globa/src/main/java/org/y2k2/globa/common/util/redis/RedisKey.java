package org.y2k2.globa.common.util.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedisKey {
    REFRESH_KEY("refreshToken:"),
    ;

    private final String value;
}
