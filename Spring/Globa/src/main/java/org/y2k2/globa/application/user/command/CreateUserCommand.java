package org.y2k2.globa.application.user.command;

import org.y2k2.globa.application.user.dto.request.RequestUserPostDTO;
import org.y2k2.globa.domain.user.type.SnsKind;

import java.security.SecureRandom;
import java.util.Random;

public record CreateUserCommand(
        SnsKind snsKind,
        String snsId,
        String name,
        String token,
        String profile,
        boolean notification,
        boolean eventNotification
) {
    public static CreateUserCommand from(RequestUserPostDTO dto) {
        return new CreateUserCommand(
                SnsKind.fromCode(dto.snsKind()),
                dto.snsId(),
                dto.name(),
                dto.token(),
                dto.profile(),
                dto.notification() != null ? dto.notification() : false,
                dto.eventNotification() != null ? dto.eventNotification() : false
        );
    }

    public boolean hasProfile() {
        return profile != null && !profile.isEmpty();
    }

    public boolean hasToken() {
        return token != null && !token.isEmpty();
    }

    public boolean isKakao() {
        return snsKind == SnsKind.KAKAO;
    }

    public boolean isGoogle() {
        return snsKind == SnsKind.GOOGLE;
    }

    public boolean isNaver() {
        return snsKind == SnsKind.NAVER;
    }

    public boolean isTwitter() {
        return snsKind == SnsKind.TWITTER;
    }

    public String generateRandomCode(){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for(int i = 0; i < 6; ++i ){
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }
}
