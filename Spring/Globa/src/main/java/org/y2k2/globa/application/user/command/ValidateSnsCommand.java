package org.y2k2.globa.application.user.command;

public record ValidateSnsCommand(
        String snsId,
        String token
) {
    public static ValidateSnsCommand of(String snsId, String token) {
        return new ValidateSnsCommand(snsId, token);
    }
}
