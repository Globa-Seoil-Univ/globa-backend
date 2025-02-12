package org.y2k2.globa.dto.request.user;

import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.annotation.ValidFile;

public record RequestProfileImageDto(
        @ValidFile(message = "프로필 이미지는 jpg, jpeg, png 형식만 가능합니다.")
        MultipartFile profile
) {
}
