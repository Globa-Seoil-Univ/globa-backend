package org.y2k2.globa.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    R("뷰어"),
    W("편집자"),
    ;

    private final String roleName;
}
