package org.y2k2.globa.application.user.command;

public record ValidateJWTCommand(
        String accessToken,
        String refreshToken
) {
    public static ValidateJWTCommand of(String accessToken, String refreshToken) {
        return new ValidateJWTCommand(accessToken, refreshToken);
    }
}
