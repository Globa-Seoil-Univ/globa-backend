package org.y2k2.globa.infrastructure.persistence.user.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.infrastructure.persistence.user.type.SnsKind;

@Slf4j
@Converter(autoApply = true)
public class SnsKindConverter implements AttributeConverter<SnsKind, String> {
    @Override
    public String convertToDatabaseColumn(SnsKind snsKind) {
        if (snsKind == null) {
            log.error("SnsKind is null");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
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
            default -> {
                log.error("Unknown SnsKind code = {}", code);
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        };
    }
}
