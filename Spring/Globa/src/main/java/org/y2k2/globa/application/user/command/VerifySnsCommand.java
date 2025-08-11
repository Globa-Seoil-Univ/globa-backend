package org.y2k2.globa.application.user.command;

public record VerifySnsCommand(
        String snsId,
        String token
) {
    public static VerifySnsCommand of(String snsId, String token) {
        return new VerifySnsCommand(snsId, token);
    }
}
