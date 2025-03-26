package org.y2k2.globa.application.user.dto.request;

import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.common.annotation.ValidFile;

public record RequestProfileImageDto(
        @ValidFile(message = "프로필 이미지는 jpg, jpeg, png 형식만 가능합니다.")
        MultipartFile profile
) {
}
