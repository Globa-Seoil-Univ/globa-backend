package org.y2k2.globa.dto.response.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ResponseUserDto implements Serializable {
    private String profile;
    private String name;
    private String code;
    private Long userId;
    private Long publicFolderId;
}