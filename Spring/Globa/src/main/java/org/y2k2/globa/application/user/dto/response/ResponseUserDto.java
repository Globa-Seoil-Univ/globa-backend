package org.y2k2.globa.application.user.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * CacheConfig.java의 activateDefaultTyping 메서드에서
 * ObjectMapper.DefaultTyping.NON_FINAL을 사용하여 직렬화할 때 클래스 정보를 포함하도록 성정해야함.
 *
 * Record 타입은 기본적으로 final이므로, 클래스 정보를 포함하지 않아
 * JsonTypeInfo.Id.CLASS를 사용하여 직렬화 시 클래스 정보를 포함해야 하는데, Service 단에서 Cache를 사용하므로
 * Record 타입을 사용하지 않고, 일반 클래스를 사용함.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ResponseUserDto {
    private String profile;
    private String name;
    private String code;
    private Long userId;
    private Long publicFolderId;
}