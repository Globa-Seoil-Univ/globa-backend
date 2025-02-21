package org.y2k2.globa.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    OWNER("소유자"),
    READER("뷰어"),
    WRITER("편집자"),
    ;

    private final String roleName;

    public static Role from(String roleName) {
        for (Role role : values()) {
            if (role.toString().equalsIgnoreCase(roleName)) {
                return role;
            }
        }

        return READER;
    }
}
