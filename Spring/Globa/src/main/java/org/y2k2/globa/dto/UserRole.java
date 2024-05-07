package org.y2k2.globa.dto;

public enum UserRole {
    ADMIN("admin"),
    EDITOR("editor"),
    VIEWER("viewer"),
    USER("user");

    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return  roleName;
    }
}
