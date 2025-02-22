package org.y2k2.globa.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.y2k2.globa.type.SnsKind;

@Converter(autoApply = true)
public class SnsKindConverter implements AttributeConverter<SnsKind, String> {
    @Override
    public String convertToDatabaseColumn(SnsKind snsKind) {
        return snsKind != null ? snsKind.getCode() : null;
    }

    @Override
    public SnsKind convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return switch (code) {
            case SnsKind.KAKAO_CODE -> SnsKind.KAKAO;
            case SnsKind.GOOGLE_CODE -> SnsKind.GOOGLE;
            default -> throw new IllegalArgumentException("Unknown SnsKind code: " + code);
        };
    }
}
