package org.y2k2.globa.infrastructure.persistence.user.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.y2k2.globa.domain.user.type.SnsKind;

@Converter(autoApply = true)
public class SnsKindConverter implements AttributeConverter<SnsKind, String> {
    @Override
    public String convertToDatabaseColumn(SnsKind snsKind) {
        if (snsKind == null) {
            throw new IllegalArgumentException("SnsKind cannot be null");
        }

        return snsKind.getCode();
    }

    @Override
    public SnsKind convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return switch (code) {
            case SnsKind.KAKAO_CODE -> SnsKind.KAKAO;
            case SnsKind.NAVER_CODE -> SnsKind.NAVER;
            case SnsKind.TWITTER_CODE -> SnsKind.TWITTER;
            case SnsKind.GOOGLE_CODE -> SnsKind.GOOGLE;
            default -> throw new IllegalArgumentException("Unknown SnsKind code: " + code);
        };
    }
}
