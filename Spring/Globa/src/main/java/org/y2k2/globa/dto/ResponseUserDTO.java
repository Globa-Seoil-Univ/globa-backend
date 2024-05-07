package org.y2k2.globa.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ResponseUserDTO implements Serializable {
    private String profile;
    private String name;
    private String code;
    private Long publicFolderId;
}