package org.y2k2.globa.application.user.dto.response;

public record ResponseUserSearchDto(
        String profile,
        String name,
        String code,
        Long userId
) {
}