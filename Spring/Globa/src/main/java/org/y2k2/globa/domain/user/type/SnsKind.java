package org.y2k2.globa.domain.user.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SnsKind {
    KAKAO("1001"),
    NAVER("1002"),
    TWITTER("1003"),
    GOOGLE("1004"),
    ;

    public static final String KAKAO_CODE = "1001";
    public static final String NAVER_CODE = "1002";
    public static final String TWITTER_CODE = "1003";
    public static final String GOOGLE_CODE = "1004";

    private final String code;

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static SnsKind fromCode(String code) {
        for (SnsKind snsKind : SnsKind.values()) {
            if (snsKind.code.equals(code)) {
                return snsKind;
            }
        }

        throw new IllegalArgumentException("Unknown SnsKind code: " + code);
    }
}
