package org.y2k2.globa.dto.request.dummy;

import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.annotation.ValidFile;

public record RequestDummyImageDto(
        @ValidFile
        MultipartFile image
) {
}
