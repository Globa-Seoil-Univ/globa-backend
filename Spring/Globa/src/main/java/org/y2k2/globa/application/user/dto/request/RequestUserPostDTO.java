package org.y2k2.globa.application.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.y2k2.globa.common.annotation.EnumValue;
import org.y2k2.globa.infrastructure.persistence.user.type.SnsKind;

public record RequestUserPostDTO(
        @EnumValue(enumClass = SnsKind.class, message = "SNS 종류는 'KAKAO', 'GOOGLE' 중 하나여야 합니다.", ignoreCase = true)
        String snsKind,

        @NotBlank(message = "SNS ID는 필수입니다.")
        String snsId,

        @Size(min = 2, max = 32, message = "이름은 2자 이상 32자 이하로 입력해주세요.")
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "Firebase 토큰은 필수입니다.")
        String token,

        String profile,
        Boolean notification,
        Boolean eventNotification
) {
}
