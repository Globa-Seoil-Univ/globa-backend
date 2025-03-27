package org.y2k2.globa.application.user.command;

public record CreateJWTCommand(
        Long userId
) {
    public static CreateJWTCommand of(Long userId) {
        return new CreateJWTCommand(userId);
    }
}
