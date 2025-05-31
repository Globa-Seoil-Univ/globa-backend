package org.y2k2.globa.application.user.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*
 * CacheConfig.java의 activateDefaultTyping 메서드에서
 * ObjectMapper.DefaultTyping.NON_FINAL을 사용하여 직렬화할 때 클래스 정보를 포함하도록 설정하였음.
 *
 * Record 타입은 기본적으로 final이므로, 클래스 정보를 포함하지 않아
 * JsonTypeInfo.Id.CLASS를 사용하여 직렬화 시 클래스 정보를 포함하도록 설정함.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record ResponseUserDto(
        String profile,
        String name,
        String code,
        Long userId,
        Long publicFolderId
) {
}