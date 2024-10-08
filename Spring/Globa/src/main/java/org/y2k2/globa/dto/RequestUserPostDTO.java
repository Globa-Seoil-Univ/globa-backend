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
public class RequestUserPostDTO implements Serializable {
    private String snsKind;
    private String snsId;
    private String name;
    private String profile;
    private String token;
    private Boolean notification;
}
