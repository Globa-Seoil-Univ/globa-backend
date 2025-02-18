package org.y2k2.globa.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    O("소유자"),
    R("뷰어"),
    W("편집자"),
    ;

    private final String roleName;

    public static Role from(String roleName) {
        for (Role role : values()) {
            if (role.toString().equalsIgnoreCase(roleName)) {
                return role;
            }
        }

        return R;
    }
}
