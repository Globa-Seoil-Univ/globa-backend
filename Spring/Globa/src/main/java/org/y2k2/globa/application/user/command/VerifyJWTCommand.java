package org.y2k2.globa.application.user.command;

public record VerifyJWTCommand(
        String accessToken,
        String refreshToken
) {
    public static VerifyJWTCommand of(String accessToken, String refreshToken) {
        return new VerifyJWTCommand(accessToken, refreshToken);
    }
}
