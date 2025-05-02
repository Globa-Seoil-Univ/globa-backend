package org.y2k2.globa.application.user.dto.response;

public record ResponseUserDto(
        String profile,
        String name,
        String code,
        Long userId,
        Long publicFolderId
) {
}