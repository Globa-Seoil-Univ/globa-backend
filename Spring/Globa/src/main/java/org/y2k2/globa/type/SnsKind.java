package org.y2k2.globa.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SnsKind {
    KAKAO("1001"),
    GOOGLE("1004"),
    ;

    public static final String KAKAO_CODE = "1001";
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
