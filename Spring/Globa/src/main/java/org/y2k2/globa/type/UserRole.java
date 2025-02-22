package org.y2k2.globa.type;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("admin"),
    EDITOR("editor"),
    VIEWER("viewer"),
    USER("user"),
    ;

    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }
}
